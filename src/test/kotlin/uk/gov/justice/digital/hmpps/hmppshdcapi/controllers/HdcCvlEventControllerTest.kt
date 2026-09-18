package uk.gov.justice.digital.hmpps.hmppshdcapi.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.NotSecuredWebMvcTest
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.HdcCvlEventController
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.HdcCvlEventPublisher
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HdcCvlQueueEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.request.HdcCvlEventRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.HdcCvlEventType
import java.time.LocalDateTime

@NotSecuredWebMvcTest(controllers = [HdcCvlEventController::class])
class HdcCvlEventControllerTest {

  @MockitoBean
  private lateinit var hdcCvlEventPublisher: HdcCvlEventPublisher

  @Autowired
  private lateinit var mvc: MockMvc

  @Autowired
  private lateinit var mapper: ObjectMapper

  @BeforeEach
  fun reset() {
    reset(hdcCvlEventPublisher)

    mvc = MockMvcBuilders
      .standaloneSetup(HdcCvlEventController(hdcCvlEventPublisher))
      .setControllerAdvice(HmppsHdcApiExceptionHandler())
      .setValidator(LocalValidatorFactoryBean().apply { afterPropertiesSet() })
      .build()
  }

  @Test
  fun `queue an HDC to CVL event`() {
    whenever(hdcCvlEventPublisher.publish(request)).thenReturn(publishedEvent)

    val result = mvc.perform(
      post("/licences/cvl-events")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request)),
    )
      .andExpect(status().isAccepted)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andReturn()

    assertThat(result.response.contentAsString)
      .isEqualTo(mapper.writeValueAsString(mapOf("eventType" to "OPT_OUT", "version" to 1, "occurredAt" to "2026-09-17T11:30:00")))

    verify(hdcCvlEventPublisher, times(1)).publish(request)
  }

  @Test
  fun `return bad request when required fields are missing`() {
    val result = mvc.perform(
      post("/licences/cvl-events")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content("""{"eventType":"OPT_OUT"}"""),
    )
      .andExpect(status().isBadRequest)
      .andReturn()

    val response: Map<String, Any?> = mapper.readValue(result.response.contentAsString)
    assertThat(response["userMessage"] as String).contains("Validation failed for one or more fields.")
  }

  private companion object {
    val request = HdcCvlEventRequest(
      eventType = HdcCvlEventType.OPT_OUT,
      licenceId = 123L,
      bookingId = 456L,
      nomsNumber = "A1234BC",
      triggeredBy = "test.user",
      reason = "Offender opted out",
    )

    val publishedEvent = HdcCvlQueueEvent(
      version = 1,
      occurredAt = LocalDateTime.of(2026, 9, 17, 11, 30, 0),
      licenceId = 123L,
      bookingId = 456L,
      nomsNumber = "A1234BC",
      triggeredBy = "test.user",
      reason = "Offender opted out",
    )
  }
}
