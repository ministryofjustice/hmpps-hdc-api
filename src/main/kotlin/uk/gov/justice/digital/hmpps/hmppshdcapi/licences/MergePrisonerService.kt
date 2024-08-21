package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

const val MERGE_EVENT_NAME = "hdc-api.prisoner.merged"

fun interface Done {
  fun complete()
}

val NO_OP = Done { }

@Service
@Transactional
class MergePrisonerService(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val telemetryClient: TelemetryClient,
  private val done: Done = NO_OP,
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
      log.debug("Updated licences and updated licence versions are empty")
      return done.complete()
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
    log.debug("Updating licences after event: {}", updatedLicences.size.toString())
    log.debug("Updating licence version after event: {}", updatedLicenceVersions.size.toString())
    return done.complete()
  }
}
