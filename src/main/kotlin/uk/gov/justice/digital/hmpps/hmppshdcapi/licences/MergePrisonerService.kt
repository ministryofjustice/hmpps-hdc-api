package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

const val MERGE_EVENT_NAME = "hdc-api.prisoner.merged"

@Service
@Transactional
class MergePrisonerService(
  private val licenceRepository: LicenceRepository,
  private val telemetryClient: TelemetryClient,
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
        log.debug("Updating licence {}", licence.id)
        licence.prisonNumber = newPrisonerNumber
        licence
      }

    licenceRepository.saveAllAndFlush(updatedLicences)

    telemetryClient.trackEvent(
      MERGE_EVENT_NAME,
      mapOf(
        "NOMS-MERGE-FROM" to oldPrisonerNumber,
        "NOMS-MERGE-TO" to newPrisonerNumber,
        "UPDATED-RECORDS" to updatedLicences.size.toString(),
      ),
      null,
    )
  }
}
