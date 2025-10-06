package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.toSAR
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Transactional(readOnly = true)
@Service
class SubjectAccessRequestService(
  val licenceRepository: LicenceRepository,
  val licenceVersionRepository: LicenceVersionRepository,
) : HmppsPrisonSubjectAccessRequestService {
  override fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent? {
    val licences = licenceRepository.findAllByPrisonNumber(prn)
    val licenceVersions = licenceVersionRepository.findAllByPrisonNumber(prn)

    return if (licences.isEmpty() && licenceVersions.isEmpty()) {
      null
    } else {
      HmppsSubjectAccessRequestContent(
        content = Content(
          licences = licences.map { it.toSAR() },
          licenceVersions = licenceVersions.map { it.toSAR() },
        ),
      )
    }
  }
}
