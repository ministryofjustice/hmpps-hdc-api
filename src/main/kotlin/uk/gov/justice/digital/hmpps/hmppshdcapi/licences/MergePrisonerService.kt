package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

const val MERGE_EVENT_NAME = "hdc-api.prisoner.merged"

fun interface EventProcessingComplete {
  fun complete()
}

val NO_OP = EventProcessingComplete { }

@Service
@Transactional
class MergePrisonerService(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val telemetryClient: TelemetryClient,
  private val eventProcessingComplete: EventProcessingComplete = NO_OP,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  fun mergePrisonerNumbers(
    oldPrisonerNumber: String,
    newPrisonerNumber: String,
  ) {
    log.info("Updating prisoner number $oldPrisonerNumber to $newPrisonerNumber")

    val updateLicenceCount = updateLicencesPrisonerNumber(oldPrisonerNumber, newPrisonerNumber)

    val updateLicenceVersionCount = updateLicenceVersionsPrisonerNumber(oldPrisonerNumber, newPrisonerNumber)

    if (updateLicenceCount == 0 && updateLicenceVersionCount == 0) {
      log.info("No licences to update")
      return eventProcessingComplete.complete()
    }

    telemetryClient.trackEvent(
      MERGE_EVENT_NAME,
      mapOf(
        "NOMS-MERGE-FROM" to oldPrisonerNumber,
        "NOMS-MERGE-TO" to newPrisonerNumber,
        "UPDATED-LICENCE-RECORDS" to updateLicenceCount.toString(),
        "UPDATED-LICENCE-VERSION-RECORDS" to updateLicenceVersionCount.toString(),
      ),
      null,
    )
    log.info("Event processing complete")
    return eventProcessingComplete.complete()
  }

  private fun updateLicencesPrisonerNumber(
    oldPrisonerNumber: String,
    newPrisonerNumber: String,
  ): Int {
    val licenceIdsToBeUpdated = licenceRepository.findAllPrisonIds(oldPrisonerNumber)
    val updatedCount = licenceRepository.updatePrisonNumber(oldPrisonerNumber, newPrisonerNumber)
    licenceIdsToBeUpdated.forEach {
      log.debug("Updating licence: {}", it)
    }
    return updatedCount
  }

  private fun updateLicenceVersionsPrisonerNumber(
    oldPrisonerNumber: String,
    newPrisonerNumber: String,
  ): Int {
    val licenceVersionIdsToBeUpdated = licenceVersionRepository.findAllPrisonIds(oldPrisonerNumber)
    val updatedCount = licenceVersionRepository.updatePrisonNumber(oldPrisonerNumber, newPrisonerNumber)
    licenceVersionIdsToBeUpdated.forEach {
      log.debug("Updating licence version: {}", it)
    }
    return updatedCount
  }
}
