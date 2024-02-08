package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository

@Service
class SubjectAccessRequestService(
  val licenceRepository: LicenceRepository,
  val licenceVersionRepository: LicenceVersionRepository,
  val auditEventRepository: AuditEventRepository,
) {
  fun getByPrisonNumber(prisonNumber: String): SarContent? {
    val licences = licenceRepository.findAllByPrisonNumber(prisonNumber)
    val licenceVersions = licenceVersionRepository.findAllByPrisonNumber(prisonNumber)

    val bookingIds = licences.map { it.bookingId } + licenceVersions.map { it.bookingId }
    val events = auditEventRepository.findByBookingIds(bookingIds.map { it.toString() }.toSet())

    return if (licences.isEmpty() && licenceVersions.isEmpty() && events.isEmpty()) {
      null
    } else {
      SarContent(
        content = Content(
          licences = licences,
          licenceVersions = licenceVersions,
          auditEvents = events,
        ),
      )
    }
  }
}
