package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.slf4j.LoggerFactory
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

  fun migrateToCvl() {
    var lastProcessedId = 0L
    var batch = 1

    while (true) {
      log.info("Processing batch {} (lastProcessedId={}, size={})", batch, lastProcessedId, BATCH_SIZE)

      val licenceIds = migrationRepository.getMigratableLicences(
        lastProcessedId = lastProcessedId,
        batchSize = BATCH_SIZE,
      )

      if (licenceIds.isEmpty()) {
        break
      }

      processBatch(licenceIds)

      lastProcessedId = licenceIds.last().licenceId
      batch++
    }
  }

  private fun processBatch(licenceDetails: List<LicenceBookingDetail>) {
    val licenceDetailsMap = licenceDetails.associateBy { it.bookingId }
    performPrisonerSearch(licenceDetails)
      .filter { migrationRequestService.isEligible(it.value) }
      .mapNotNull { (bookingId, prisoner) -> licenceDetailsMap[bookingId]!! to prisoner }
      .forEach { (licenceDetail, prisoner) ->
        processBatchedLicence(licenceDetail, prisoner)
        sleep(100.milliseconds.inWholeMilliseconds)
      }
  }

  private fun processBatchedLicence(licenceDetail: LicenceBookingDetail, prisoner: Prisoner) {
    try {
      migrationRequestService.migrateBatchedLicenceToCvl(licenceDetail, prisoner)
      logSuccess(licenceDetail.licenceId)
    } catch (e: CvlRetryMigrationException) {
      logFailure(licenceDetail.licenceId, e, retry = true, MigrationErrorSource.CVL)
    } catch (e: CvlMigrationException) {
      logFailure(licenceDetail.licenceId, e, retry = false, MigrationErrorSource.CVL)
    } catch (e: MigrationValidationException) {
      logFailure(licenceDetail.licenceId, e, retry = false, MigrationErrorSource.HDC)
    } catch (e: Exception) {
      logFailure(licenceDetail.licenceId, e, retry = false, MigrationErrorSource.HDC)
    }
  }

  fun processLicence(licenceId: Long) {
    try {
      migrationRequestService.migrateLicenceToCvl(licenceId)
      logSuccess(licenceId)
    } catch (e: CvlRetryMigrationException) {
      logFailure(licenceId, e, retry = true, MigrationErrorSource.CVL)
      throw e
    } catch (e: CvlMigrationException) {
      logFailure(licenceId, e, retry = false, MigrationErrorSource.CVL)
      throw e
    } catch (e: MigrationValidationException) {
      logFailure(licenceId, e, retry = false, MigrationErrorSource.HDC)
      throw e
    } catch (e: Exception) {
      logFailure(licenceId, e, retry = false, MigrationErrorSource.HDC)
      throw e
    }
  }

  private fun performPrisonerSearch(licenceDetails: List<LicenceBookingDetail>): Map<Long, Prisoner> {
    val bookingIds = licenceDetails.map { it.bookingId }.toList()
    val prisoners = prisonSearchApiClient.getPrisonersByBookingIds(bookingIds).associateBy { it.bookingId.toLong() }
    licenceDetails.forEach { licenceDetail ->
      if (!prisoners.containsKey(licenceDetail.bookingId)) {
        logFailure(licenceDetail.licenceId, "Prisoner not found for booking id ${licenceDetail.bookingId}", retry = false, MigrationErrorSource.HDC)
      }
    }
    return prisoners
  }

  private fun logSuccess(licenceId: Long) {
    log.info("Licence id: $licenceId, migrated successfully")
    migrationRepository.insertMigrationLog(licenceId, true, retry = false, "migrated successfully")
  }

  private fun logFailure(licenceId: Long, e: Exception, retry: Boolean, source: MigrationErrorSource) {
    log.debug("Licence id: $licenceId, error: ${e.message}", e)
    logFailure(licenceId, e.message ?: "Unknown error", retry, source)
  }

  private fun logFailure(licenceId: Long, message: String, retry: Boolean, source: MigrationErrorSource) {
    migrationRepository.insertMigrationLog(licenceId, false, retry = retry, message, source.name)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private const val BATCH_SIZE = 1000
  }
}
