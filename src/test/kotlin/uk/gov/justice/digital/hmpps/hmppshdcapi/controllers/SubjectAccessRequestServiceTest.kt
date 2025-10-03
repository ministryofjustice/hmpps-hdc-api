package uk.gov.justice.digital.hmpps.hmppshdcapi.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.aCas2ApprovedPremisesLicence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.aLicenceVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.Content
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.SubjectAccessRequestService
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.toSAR
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent


class SubjectAccessRequestServiceTest {
  private val licenceRepository: LicenceRepository = mock()
  private val licenceVersionRepository: LicenceVersionRepository = mock()

  private val service = SubjectAccessRequestService(licenceRepository, licenceVersionRepository)
  private val mapper = ObjectMapper()

//  @Autowired
//  private lateinit var mapper: ObjectMapper

  @Test
  fun `throws entity not found`() {
    whenever(licenceRepository.findAllByPrisonNumber(any())).thenReturn(emptyList())

    assertThat(service.getPrisonContentFor("A12345", null, null)).isNull()
  }

  @Test
  fun `by prison number`() {
    whenever(licenceRepository.findAllByPrisonNumber("T1234TS")).thenReturn(listOf(aCas2ApprovedPremisesLicence()))
    whenever(licenceVersionRepository.findAllByPrisonNumber("T1234TS")).thenReturn(listOf(aLicenceVersion()))

    // Get actual response from service
    val actualResponse = service.getPrisonContentFor("T1234TS", null, null)

    // Serialize both expected and actual to JSON
    val expectedJson = mapper.writeValueAsString(sarContentResponse)
    val actualJson = mapper.writeValueAsString(actualResponse)

    // Compare JSON strings
    assertThat(actualJson).isEqualTo(expectedJson)
  }

  private companion object {
    val sarContentResponse = HmppsSubjectAccessRequestContent(
      Content(
        licences = listOf(aCas2ApprovedPremisesLicence().toSAR()),
        licenceVersions = listOf(aLicenceVersion().toSAR()),
      ),
    )
  }
}
