package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlRetryMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.LicenceBookingDetail
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationErrorSource
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.lang.Thread.sleep
import kotlin.time.Duration.Companion.milliseconds

@Transactional(propagation = Propagation.NEVER)
@Service
class MigrationProcessService(
  private val migrationRepository: MigrationRepository,
  private val migrationRequestService: MigrationRequestService,
  private val prisonSearchApiClient: PrisonSearchApiClient,
) {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  @Async
  fun migrateToCvl() {
    var lastProcessedId = 0L
    var batch = 1

    try {
      var licenceVersionIds: List<LicenceBookingDetail>
      do {
        log.info("HDC migration: Processing batch {} (lastProcessedId={}, size={})", batch, lastProcessedId, BATCH_SIZE)

        licenceVersionIds = migrationRepository.getMigratableLicences(
          lastProcessedId = lastProcessedId,
          batchSize = BATCH_SIZE,
        )
        log.info("HDC migration: Fetched {} licence", licenceVersionIds.size)

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
          processBatchedLicence(licenceDetail, prisoner)
          sleep(100.milliseconds.inWholeMilliseconds)
        }
    } finally {
      // To prevent out of memory issues
      entityManager.clear()
    }
  }

  private fun processBatchedLicence(licenceDetail: LicenceBookingDetail, prisoner: Prisoner) {
    log.info("HDC migration: Processing licence version id {}", licenceDetail.licenceVersionId)
    try {
      migrationRequestService.validate(prisoner)
      migrationRequestService.migrateBatchedLicenceToCvl(licenceDetail, prisoner)
      logSuccess(licenceDetail.licenceVersionId, licenceDetail.bookingId)
    } catch (e: CvlRetryMigrationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = true, MigrationErrorSource.CVL)
    } catch (e: CvlMigrationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = false, MigrationErrorSource.CVL)
    } catch (e: MigrationValidationException) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = false, MigrationErrorSource.HDC)
    } catch (e: Exception) {
      logFailure(licenceDetail.licenceVersionId, licenceDetail.bookingId, e, retry = false, MigrationErrorSource.HDC)
    }
  }

  fun processLicence(licenceVersionId: Long) {
    val licenceBookingDetail = migrationRepository.getMigratableLicenceDetails(licenceVersionId) ?: throw MigrationValidationException("No eligible licence found for licence version id $licenceVersionId")
    try {
      migrationRequestService.migrateLicenceToCvl(licenceVersionId)
      logSuccess(licenceVersionId, licenceBookingDetail.bookingId)
    } catch (e: CvlRetryMigrationException) {
      logFailure(licenceVersionId, licenceBookingDetail.bookingId, e, retry = true, MigrationErrorSource.CVL)
      throw e
    } catch (e: CvlMigrationException) {
      logFailure(licenceVersionId, licenceBookingDetail.bookingId, e, retry = false, MigrationErrorSource.CVL)
      throw e
    } catch (e: MigrationValidationException) {
      logFailure(licenceVersionId, licenceBookingDetail.bookingId, e, retry = false, MigrationErrorSource.HDC)
      throw e
    } catch (e: Exception) {
      logFailure(licenceVersionId, licenceBookingDetail.bookingId, e, retry = false, MigrationErrorSource.HDC)
      throw e
    }
  }

  private fun performPrisonerSearchByBookingId(licenceDetails: List<LicenceBookingDetail>): Map<Long, Prisoner> {
    log.info("HDC migration: Fetching prisoner details for booking ids {}", licenceDetails.map { it.bookingId })
    val bookingIds = licenceDetails.map { it.bookingId }.toList()

    try {
      val prisoners = prisonSearchApiClient.getPrisonersByBookingIds(bookingIds).associateBy { it.bookingId.toLong() }
      licenceDetails.forEach { licenceDetail ->
        if (!prisoners.containsKey(licenceDetail.bookingId)) {
          logFailure(
            licenceDetail.licenceVersionId,
            licenceDetail.bookingId,
            "Prisoner not found for booking id ${licenceDetail.bookingId}",
            retry = false,
            MigrationErrorSource.HDC,
          )
        }
      }
      return prisoners
    } catch (e: Exception) {
      log.error("HDC migration: Error fetching prisoner details for booking ids $bookingIds", e)
      throw e
    }
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
              "Not Active Booking id, prisoner booking id :${it.bookingId} != licence booking id: ${licenceDetail.bookingId}",
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
    log.info("HDC migration: Licence id: $licenceVersionId, migrated successfully")
    migrationRepository.insertMigrationLog(licenceVersionId, bookingId, true, retry = false, "migrated successfully")
  }

  private fun logFailure(licenceVersionId: Long, bookingId: Long, e: Exception, retry: Boolean, source: MigrationErrorSource) {
    log.debug("HDC migration: Licence id: $licenceVersionId, error: ${e.message}", e)
    logFailure(licenceVersionId, bookingId, e.message ?: e::class.simpleName ?: "Unknown error", retry, source)
  }

  private fun logFailure(licenceVersionId: Long, bookingId: Long, message: String, retry: Boolean, source: MigrationErrorSource) {
    migrationRepository.insertMigrationLog(licenceVersionId, bookingId, false, retry = retry, message, source.name)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    // The maximum number of licenses we can process is 999 as the prisoners by booking ids must have between 1 and 1000 {
    private const val BATCH_SIZE = 100
  }
}
