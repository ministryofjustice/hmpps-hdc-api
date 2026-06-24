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
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.LicenceBookingDetail
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationErrorSource
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
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
  private val allowedMigrationDate: LocalDate?,
  private val clock: Clock = Clock.systemDefaultZone(),
) {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  @Async
  fun migrateABatchOfLicences() {
    if (checkIfMigrationIsAllowed()) return

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
      performPrisonerSearchByPrisonNumber(licenceDetails)
        .filter { (bookingId, _) -> licenceDetailsMap.containsKey(bookingId) }
        .mapNotNull { (bookingId, prisoner) -> licenceDetailsMap[bookingId]!! to prisoner }
        .forEach { (licenceDetail, prisoner) ->
          processLicence(licenceDetail, prisoner)
          sleep(125.milliseconds.inWholeMilliseconds)
        }
    } finally {
      // To prevent out of memory issues
      entityManager.clear()
    }
  }

  fun migrateALicence(bookingId: Long) {
    try {
      val licenceBookingDetail = migrationRepository.getMigratableLicenceDetails(bookingId)
        ?: throw MigrationLicenceVersionNotFoundException("No eligible licence version found for booking Id $bookingId")
      val prisoner = migrationRequestService.performPrisonerSearch(licenceBookingDetail.bookingId)
      processLicence(licenceBookingDetail, prisoner, true)
    } catch (e: MigrationLicenceVersionNotFoundException) {
      logFailure(null, bookingId, e, retry = true, MigrationErrorSource.HDC)
      throw e
    }
  }

  private fun processLicence(licenceDetail: LicenceBookingDetail, prisoner: Prisoner, throwExceptions: Boolean = false) {
    log.info("HDC migration: Processing licence version id {}", licenceDetail.licenceVersionId)
    try {
      migrationRequestService.validate(prisoner)
      migrationRequestService.migrateLicenceToCvl(licenceDetail, prisoner)
      logSuccess(licenceDetail.licenceVersionId, licenceDetail.bookingId)
    } catch (e: CvlRetryMigrationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = true, MigrationErrorSource.CVL)
      if (throwExceptions) throw e
    } catch (e: CvlMigrationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = false, MigrationErrorSource.CVL)
      if (throwExceptions) throw e
    } catch (e: MigrationValidationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = false, MigrationErrorSource.HDC)
      if (throwExceptions) throw e
    } catch (e: PrematureCloseException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = true, MigrationErrorSource.HDC)
      if (throwExceptions) throw e
    } catch (e: Errors.NativeIoException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = true, MigrationErrorSource.HDC)
      if (throwExceptions) throw e
    } catch (e: WebClientRequestException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = true, MigrationErrorSource.HDC)
      if (throwExceptions) throw e
    } catch (e: Exception) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = false, MigrationErrorSource.HDC)
      if (throwExceptions) throw e
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

  private fun performPrisonerSearchByPrisonNumber(licenceDetails: List<LicenceBookingDetail>): Map<Long, Prisoner> {
    log.info("HDC migration: Fetching prisoner details for prison number {}", licenceDetails.map { it.bookingId })
    val prisonNumbers = licenceDetails.map { it.prisonNumber }.toList()

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
              "Old booking id in hdc, ${licenceDetail.bookingId} != ${it.bookingId} prisoner booking id, status: ${it.status}",
              retry = false,
              MigrationErrorSource.HDC,
            )
          }
        } ?: run {
          logFailure(
            licenceDetail.licenceVersionId,
            licenceDetail.bookingId,
            "Prisoner not found for prisoner number ${licenceDetail.prisonNumber}",
            retry = false,
            MigrationErrorSource.HDC,
          )
        }
      }

      return prisoners.associateBy { it.bookingId.toLong() }
    } catch (e: Exception) {
      log.error("HDC migration: Error fetching prisoner details for prison numbers $prisonNumbers", e)
      throw e
    }
  }

  private fun logSuccess(licenceVersionId: Long, bookingId: Long) {
    log.info("HDC migration: Licence version id: $licenceVersionId, migrated successfully")
    migrationRepository.insertMigrationLog(licenceVersionId, bookingId, true, retry = false, "migrated successfully")
  }

  private fun logFailure(licenceVersionId: Long? = null, bookingId: Long, e: Exception, retry: Boolean, source: MigrationErrorSource) {
    log.debug("HDC migration: Licence version id: $licenceVersionId, error: ${e.message}", e)
    logFailure(licenceVersionId, bookingId, e.message ?: e::class.simpleName ?: "Unknown error", retry, source)
  }

  private fun logFailure(licenceVersionId: Long? = null, bookingId: Long, message: String, retry: Boolean, source: MigrationErrorSource) {
    migrationRepository.insertMigrationLog(licenceVersionId, bookingId, false, retry = retry, message, source.name)
  }

  private fun checkIfMigrationIsAllowed(): Boolean {
    if (allowedMigrationDate == null) {
      log.info("HDC migration: Migration to cvl is skipped because migration date is not configured")
      return true
    }
    if (!isMigrationAllowed()) {
      log.info(
        "HDC migration:  Migration to cvl is skipped because migration {} date has not been reached",
        allowedMigrationDate,
      )
      return true
    }
    return false
  }

  fun isMigrationAllowed(): Boolean = allowedMigrationDate?.let { !getCurrentDate().isBefore(it) } ?: false
  private fun getCurrentDate(): LocalDate = LocalDate.now(clock)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    // The maximum number of licenses we can process is 999 as the prisoners by booking ids must have between 1 and 1000 {
    private const val BATCH_SIZE = 100
  }
}
