package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HdcCvlQueueEvent

class HdcCvlEventIntegrationTest : SqsIntegrationTestBase() {

  @Test
  fun `receive request from HDC and raise event to queue`() {
    val requestBody = mapOf(
      "eventType" to "OPT_OUT",
      "licenceId" to 123,
      "bookingId" to 456,
      "nomsNumber" to "A1234BC",
      "triggeredBy" to "test.user",
      "reason" to "Offender opted out",
    )

    webTestClient.post()
      .uri("/licences/cvl-events")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isAccepted
      .expectBody()
      .jsonPath("$.eventType").isEqualTo("OPT_OUT")

    await untilCallTo { getNumberOfMessagesCurrentlyOnHdcCvlQueue() } matches { it == 1 }

    val response = hdcCvlEventsQueue.sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(hdcCvlEventsQueue.queueUrl)
        .maxNumberOfMessages(1)
        .messageAttributeNames("All")
        .build(),
    ).get()
    val message = response.messages().single()
    val event = objectMapper.readValue(message.body(), HdcCvlQueueEvent::class.java)

    assertThat(message.messageAttributes()["eventType"]?.stringValue()).isEqualTo("OPT_OUT")
    assertThat(event.licenceId).isEqualTo(123)
    assertThat(event.bookingId).isEqualTo(456)
    assertThat(event.nomsNumber).isEqualTo("A1234BC")
    assertThat(event.triggeredBy).isEqualTo("test.user")
    assertThat(event.reason).isEqualTo("Offender opted out")
  }

  @Test
  fun `return forbidden when incorrect roles are supplied`() {
    val result = webTestClient.post()
      .uri("/licences/cvl-events")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
      .bodyValue(validRequestBody())
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN.value())
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).contains("Access Denied")
  }

  @Test
  fun `return bad request when request is invalid`() {
    val result = webTestClient.post()
      .uri("/licences/cvl-events")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .bodyValue(mapOf("eventType" to "OPT_OUT"))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).contains("Malformed JSON request.")
  }

  @Test
  fun `return bad request when required string fields are blank`() {
    val result = webTestClient.post()
      .uri("/licences/cvl-events")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .bodyValue(
        mapOf(
          "eventType" to "OPT_OUT",
          "licenceId" to 123,
          "bookingId" to 456,
          "nomsNumber" to "   ",
          "triggeredBy" to "   ",
        ),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).isEqualTo("Validation failed for one or more fields.")
    assertThat(result?.developerMessage).contains("nomsNumber must be supplied")
    assertThat(result?.developerMessage).contains("triggeredBy must be supplied")
  }

  @Test
  fun `return bad request when event type is invalid`() {
    val result = webTestClient.post()
      .uri("/licences/cvl-events")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .bodyValue(
        mapOf(
          "eventType" to "INVALID",
          "licenceId" to 123,
          "bookingId" to 456,
          "nomsNumber" to "A1234BC",
          "triggeredBy" to "test.user",
        ),
      )
      .exchange()
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).contains("Malformed JSON request.")
    assertThat(result?.developerMessage).contains("HdcCvlEventType")
  }

  private fun validRequestBody() = mapOf(
    "eventType" to "OPT_OUT",
    "licenceId" to 123,
    "bookingId" to 456,
    "nomsNumber" to "A1234BC",
    "triggeredBy" to "test.user",
    "reason" to "Offender opted out",
  )
}
