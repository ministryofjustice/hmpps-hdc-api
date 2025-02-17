package uk.gov.justice.digital.hmpps.hmppshdcapi.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.Content
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.SarContent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.SubjectAccessRequestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests.SubjectAccessRequestService

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@WebMvcTest(controllers = [SubjectAccessRequestController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SubjectAccessRequestController::class])
@WebAppConfiguration
class SubjectAccessRequestControllerTest {

  @MockitoBean
  private lateinit var subjectAccessRequestService: SubjectAccessRequestService

  @Autowired
  private lateinit var mvc: MockMvc

  @Autowired
  private lateinit var mapper: ObjectMapper

  @BeforeEach
  fun reset() {
    reset(subjectAccessRequestService)

    mvc = MockMvcBuilders
      .standaloneSetup(SubjectAccessRequestController(subjectAccessRequestService))
      .setControllerAdvice(HmppsHdcApiExceptionHandler())
      .build()
  }

  @Test
  fun `get a Sar Content by id returns ok and have a response`() {
    whenever(subjectAccessRequestService.getByPrisonNumber("G4169UO")).thenReturn(sarContentResponse)
    val result = mvc.perform(get("/subject-access-request?prn=G4169UO").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andReturn()

    assertThat(result.response.contentAsString)
      .isEqualTo(mapper.writeValueAsString(sarContentResponse))
  }

  @Test
  fun `500 when pass both prn and crn`() {
    val result =
      mvc.perform(get("/subject-access-request?prn=G4169UO&crn=Z265290").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError)
        .andReturn()

    assertThat(result.response.contentAsString).contains("Only supports search by single identifier.")

    verify(subjectAccessRequestService, times(0)).getByPrisonNumber("G4169UO")
  }

  @Test
  fun `209 when pass crn and but not prn`() {
    val result =
      mvc.perform(get("/subject-access-request?crn=Z265290").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().`is`(209))
        .andReturn()

    assertThat(result.response.contentAsString).contains("Search by crn is not supported.")

    verify(subjectAccessRequestService, times(0)).getByPrisonNumber("G4169UO")
  }

  @Test
  fun `204 when pass prn but no records found`() {
    whenever(subjectAccessRequestService.getByPrisonNumber("G4169UO")).thenReturn(null)
    val result =
      mvc.perform(get("/subject-access-request?prn=G4169UO").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent)
        .andReturn()

    assertThat(result.response.contentAsString).contains("No records found for the prn.")

    verify(subjectAccessRequestService, times(1)).getByPrisonNumber("G4169UO")
  }

  private companion object {
    val sarContentResponse = SarContent(
      Content(
        licences = emptyList(),
        licenceVersions = emptyList(),
      ),
    )
  }
}
