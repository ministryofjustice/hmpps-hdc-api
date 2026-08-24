package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import io.netty.channel.unix.Errors
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.netty.http.client.PrematureCloseException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlRetryMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationLicenceVersionNotFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationPrisonerNotFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.FailedMigrationSummary
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.LicenceBookingDetail
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationErrorSource
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationTrigger
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.response.LicenceMigrationLogEntryDto
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.lang.Thread.sleep
import java.time.Clock
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@Transactional(propagation = Propagation.NEVER)
@Service
class MigrationProcessService(
  private val migrationRepository: MigrationRepository,
  private val migrationRequestService: MigrationRequestService,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  @param:Value("\${feature.toggle.cvl.migration.date:#{null}}")
  private val allowedNroMigrationDate: LocalDate?,
  private val clock: Clock = Clock.systemDefaultZone(),
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  @Async
  fun migrateABatchOfLicences() {
    if (!checkIfNroMigrationIsAllowed()) return

    var lastProcessedId = 0L
    var batch = 1

    try {
      var licenceVersionIds: List<LicenceBookingDetail>
      do {
        log.info("HDC migration: Processing batch {} (lastProcessedId={}, size={})", batch, lastProcessedId, BATCH_SIZE)

        licenceVersionIds = migrationRepository.getMigratableLicenceBatch(
          lastProcessedId = lastProcessedId,
          batchSize = BATCH_SIZE,
        )
        log.info("HDC migration: Fetched {} licences", licenceVersionIds.size)

        if (licenceVersionIds.isEmpty()) {
          break
        }
        processBatch(licenceVersionIds)

        lastProcessedId = licenceVersionIds.last().licenceVersionId
        log.info("HDC migration:  Processed batch {} (lastProcessedId={})", batch, lastProcessedId)
        batch++
      } while (licenceVersionIds.size == BATCH_SIZE)

      log.info("HDC migration: Finished all batches!")
    } catch (e: Exception) {
      log.error("HDC migration: Error processing batch :{} lastProcessedId{}", batch, lastProcessedId, e)
      throw e
    }
  }

  private fun processBatch(licenceDetails: List<LicenceBookingDetail>) {
    try {
      val licenceDetailsMap = licenceDetails.associateBy { it.bookingId }
      performPrisonerSearchByPrisonNumber(licenceDetails, migrationTrigger = MigrationTrigger.BATCH)
        .filter { (bookingId, _) -> licenceDetailsMap.containsKey(bookingId) }
        .mapNotNull { (bookingId, prisoner) -> licenceDetailsMap[bookingId]!! to prisoner }
        .forEach { (licenceDetail, prisoner) ->
          processLicence(licenceDetail, prisoner, migrationTrigger = MigrationTrigger.BATCH)
          sleep(125.milliseconds.inWholeMilliseconds)
        }
    } finally {
      // To prevent out of memory issues
      entityManager.clear()
    }
  }

  fun migrateALicence(bookingId: Long) {
    var prisoner: Prisoner? = null
    var licenceBookingDetail: LicenceBookingDetail? = null
    try {
      licenceBookingDetail = migrationRepository.getMigratableLicenceDetails(bookingId, ignoreRetry = true)
        ?: throw MigrationLicenceVersionNotFoundException("No eligible licence version found for booking Id $bookingId")
      prisoner = migrationRequestService.performPrisonerSearch(licenceBookingDetail.bookingId)
      processLicence(licenceBookingDetail, prisoner, throwAllExceptions = true, migrationTrigger = MigrationTrigger.USER)
    } catch (e: MigrationLicenceVersionNotFoundException) {
      logFailure(null, bookingId, prisoner, e, retry = true, MigrationErrorSource.HDC, migrationTrigger = MigrationTrigger.USER)
      throw e
    } catch (e: MigrationPrisonerNotFoundException) {
      logFailure(
        licenceBookingDetail?.licenceVersionId,
        bookingId,
        licenceBookingDetail?.prisonNumber,
        e.message!!,
        retry = true,
        MigrationErrorSource.HDC,
        migrationTrigger = MigrationTrigger.USER,
      )
      throw e
    }
  }

  fun migrateALicenceForPrisonerReleaseEvent(prisonNumber: String) {
    if (!checkIfNroMigrationIsAllowed()) return

    try {
      val prisoner = prisonSearchApiClient.getPrisonersByPrisonNumber(listOf(prisonNumber)).firstOrNull()
        ?: throw MigrationPrisonerNotFoundException("Prisoner not found for prison number $prisonNumber")
      log.info("HDC migration event: Release Event, Prisoner {}", prisonNumber)

      val bookingId = prisoner.bookingId.toLong()
      migrationRepository.getMigratableLicenceDetails(bookingId, ignoreRetry = true)?.let {
        processLicence(it, prisoner, throwRetryableExceptions = true, migrationTrigger = MigrationTrigger.EVENT)
      }
    } catch (e: MigrationPrisonerNotFoundException) {
      log.info("HDC migration: Release Event, {}", e.message)
    }
  }

  private fun processLicence(
    licenceDetail: LicenceBookingDetail,
    prisoner: Prisoner,
    throwAllExceptions: Boolean = false,
    throwRetryableExceptions: Boolean = false,
    migrationTrigger: MigrationTrigger,
  ) {
    log.info("HDC migration: Processing licence version id {}", licenceDetail.licenceVersionId)
    try {
      migrationRequestService.validate(prisoner)
      migrationRequestService.migrateLicenceToCvl(licenceDetail, prisoner)
      logSuccess(licenceDetail.licenceVersionId, licenceDetail.bookingId, licenceDetail.prisonNumber, migrationTrigger)
    } catch (e: CvlRetryMigrationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = true, MigrationErrorSource.CVL, migrationTrigger)
      if (throwAllExceptions || throwRetryableExceptions) throw e
    } catch (e: CvlMigrationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = false, MigrationErrorSource.CVL, migrationTrigger)
      if (throwAllExceptions) throw e
    } catch (e: MigrationValidationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = false, MigrationErrorSource.HDC, migrationTrigger)
      if (throwAllExceptions) throw e
    } catch (e: PrematureCloseException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = true, MigrationErrorSource.HDC, migrationTrigger)
      if (throwAllExceptions || throwRetryableExceptions) throw e
    } catch (e: Errors.NativeIoException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = true, MigrationErrorSource.HDC, migrationTrigger)
      if (throwAllExceptions || throwRetryableExceptions) throw e
    } catch (e: WebClientRequestException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = true, MigrationErrorSource.HDC, migrationTrigger)
      if (throwAllExceptions || throwRetryableExceptions) throw e
    } catch (e: Exception) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, prisoner, e, retry = false, MigrationErrorSource.HDC, migrationTrigger)
      if (throwAllExceptions) throw e
    }
  }

  fun getMigrationLogs(
    licenceVersionId: Long?,
    bookingId: Long?,
    errorSource: String?,
    success: Boolean?,
    pageable: Pageable,
  ): Page<LicenceMigrationLogEntryDto> {
    log.info(
      "HDC migration: Fetching migration logs with filters - licenceVersionId: {}, bookingId: {}, errorSource: {}, success: {}",
      licenceVersionId,
      bookingId,
      errorSource,
      success,
    )
    return migrationRepository.getMigrationLogs(licenceVersionId, bookingId, errorSource, success, pageable)
  }

  @Transactional
  fun updateRetryState(logId: Long, retry: Boolean) {
    log.info("HDC migration: Updating retry state for log id: $logId, retry: $retry")
    migrationRepository.updateRetryState(logId, retry)
  }

  fun getRepeatedFailedMigrations(): List<FailedMigrationSummary> = migrationRepository.findRepeatedFailedMigrations()

  private fun performPrisonerSearchByPrisonNumber(
    licenceDetails: List<LicenceBookingDetail>,
    migrationTrigger: MigrationTrigger,
  ): Map<Long, Prisoner> {
    log.info("HDC migration: Fetching prisoner details for prison number {}", licenceDetails.map { it.bookingId })
    val prisonNumbers = licenceDetails.map { it.prisonNumber }

    try {
      val prisoners = prisonSearchApiClient.getPrisonersByPrisonNumber(prisonNumbers)
      val prisonersMap = prisoners.associateBy { it.prisonerNumber }

      licenceDetails.forEach { licenceDetail ->

        val prisoner = prisonersMap[licenceDetail.prisonNumber]

        prisoner?.let {
          if (it.bookingId.toLong() != licenceDetail.bookingId) {
            logFailure(
              licenceDetail.licenceVersionId,
              licenceDetail.bookingId,
              licenceDetail.prisonNumber,
              "Old booking id in hdc, ${licenceDetail.bookingId} != ${it.bookingId} prisoner booking id, status: ${it.status}",
              retry = false,
              MigrationErrorSource.HDC,
              migrationTrigger,
            )
          }
        } ?: run {
          logFailure(
            licenceDetail.licenceVersionId,
            licenceDetail.bookingId,
            licenceDetail.prisonNumber,
            "Prisoner not found for prisoner number ${licenceDetail.prisonNumber}",
            retry = false,
            MigrationErrorSource.HDC,
            migrationTrigger,
          )
        }
      }

      return prisoners.associateBy { it.bookingId.toLong() }
    } catch (e: Exception) {
      log.error("HDC migration: Error fetching prisoner details for prison numbers $prisonNumbers", e)
      throw e
    }
  }

  private fun logSuccess(licenceVersionId: Long, bookingId: Long, prisonNumber: String, migrationTrigger: MigrationTrigger) {
    log.info("HDC migration: Licence version id: $licenceVersionId, migrated successfully")
    migrationRepository.insertMigrationLog(
      licenceVersionId,
      bookingId,
      prisonNumber,
      true,
      retry = false,
      "migrated successfully",
      migrationTrigger = migrationTrigger.name,
    )
    migrationRepository.updateMigrationStateById(licenceVersionId, "COMPLETED")
  }

  private fun logFailure(licenceVersionId: Long? = null, bookingId: Long? = null, prisoner: Prisoner? = null, e: Exception, retry: Boolean, source: MigrationErrorSource, migrationTrigger: MigrationTrigger) {
    log.debug("HDC migration: Licence version id: $licenceVersionId, error: ${e.message}", e)
    var message = e.message ?: e::class.simpleName ?: "Unknown error"
    prisoner?.let {
      with(it) {
        message += ", status:${it.status} Ard:$confirmedReleaseDate Crd:$conditionalReleaseDate Led:$licenceExpiryDate Hdcad:$homeDetentionCurfewActualDate"
      }
    }
    logFailure(licenceVersionId, bookingId, prisoner?.prisonerNumber, message, retry, source, migrationTrigger)
  }

  private fun logFailure(licenceVersionId: Long? = null, bookingId: Long? = null, prisonNumber: String? = null, message: String, retry: Boolean, source: MigrationErrorSource, migrationTrigger: MigrationTrigger) {
    migrationRepository.insertMigrationLog(licenceVersionId, bookingId, prisonNumber, false, retry = retry, message, source.name, migrationTrigger.name)
    licenceVersionId?.let {
      migrationRepository.updateMigrationStateById(licenceVersionId, "FAILED")
    }
  }

  private fun checkIfNroMigrationIsAllowed(): Boolean {
    if (allowedNroMigrationDate == null) {
      log.info("HDC migration: NRO Migration to cvl is skipped because migration date is not configured")
      return false
    }
    if (!isMigrationAllowed()) {
      log.info(
        "HDC migration: NRO Migration to cvl is skipped because migration {} date has not been reached",
        allowedNroMigrationDate,
      )
      return false
    }
    return true
  }

  fun isMigrationAllowed(): Boolean = allowedNroMigrationDate?.let { !getCurrentDate().isBefore(it) } ?: false
  private fun getCurrentDate(): LocalDate = LocalDate.now(clock)

  companion object {
    // The maximum number of licenses we can process is 999 as the prisoners by booking ids must have between 1 and 1000 {
    private const val BATCH_SIZE = 100
  }
}
