package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.toSAR

@Service
class SubjectAccessRequestService(
  val licenceRepository: LicenceRepository,
  val licenceVersionRepository: LicenceVersionRepository,
) {
  fun getByPrisonNumber(prisonNumber: String): SarContent? {
    val licences = licenceRepository.findAllByPrisonNumber(prisonNumber)
    val licenceVersions = licenceVersionRepository.findAllByPrisonNumber(prisonNumber)

    return if (licences.isEmpty() && licenceVersions.isEmpty()) {
      null
    } else {
      SarContent(
        content = Content(
          licences = licences.map { it.toSAR() },
          licenceVersions = licenceVersions.map { it.toSAR() },
        ),
      )
    }
  }
}
