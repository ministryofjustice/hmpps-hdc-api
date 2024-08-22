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

  fun mergePrisonerNumbers(
    oldPrisonerNumber: String,
    newPrisonerNumber: String,
  ) {
    log.info("Updating prisoner number $oldPrisonerNumber to $newPrisonerNumber")

    val updatedLicences = licenceRepository.findAllByPrisonNumber(oldPrisonerNumber)
      .map { licence ->
        log.debug("Updating licence: {}", licence.id)
        licence.prisonNumber = newPrisonerNumber
        licence
      }

    val updatedLicenceVersions = licenceVersionRepository.findAllByPrisonNumber(oldPrisonerNumber)
      .map { licenceVersion ->
        log.debug("Updating licence version: {}", licenceVersion.id)
        licenceVersion.prisonNumber = newPrisonerNumber
        licenceVersion
      }

    if (updatedLicences.isEmpty() && updatedLicenceVersions.isEmpty()) {
      log.info("No licences to update")
      return eventProcessingComplete.complete()
    }

    if (updatedLicences.isNotEmpty()) licenceRepository.saveAllAndFlush(updatedLicences)
    if (updatedLicenceVersions.isNotEmpty()) licenceVersionRepository.saveAllAndFlush(updatedLicenceVersions)

    telemetryClient.trackEvent(
      MERGE_EVENT_NAME,
      mapOf(
        "NOMS-MERGE-FROM" to oldPrisonerNumber,
        "NOMS-MERGE-TO" to newPrisonerNumber,
        "UPDATED-LICENCE-RECORDS" to updatedLicences.size.toString(),
        "UPDATED-LICENCE-VERSION-RECORDS" to updatedLicenceVersions.size.toString(),
      ),
      null,
    )
    log.info("Event processing complete")
    return eventProcessingComplete.complete()
  }
}
