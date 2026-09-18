package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
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
      .jsonPath("$.version").isEqualTo(1)

    await untilCallTo { getNumberOfMessagesCurrentlyOnHdcCvlQueue() } matches { it == 1 }

    val response = hdcCvlEventsQueue.sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(hdcCvlEventsQueue.queueUrl)
        .maxNumberOfMessages(1)
        .messageAttributeNames("All")
        .build(),
    ).get()
    val message = response.messages().single()

    assertThat(message.messageAttributes()["eventType"]?.stringValue()).isEqualTo("OPT_OUT")
    assertThatJson(message.body())
      .inPath("$.licenceId").isEqualTo(123)
    assertThatJson(message.body())
      .inPath("$.bookingId").isEqualTo(456)
    assertThatJson(message.body())
      .inPath("$.nomsNumber").isEqualTo("A1234BC")
    assertThatJson(message.body())
      .inPath("$.triggeredBy").isEqualTo("test.user")
    assertThatJson(message.body())
      .inPath("$.reason").isEqualTo("Offender opted out")
    assertThatJson(message.body())
      .inPath("$.version").isEqualTo(1)
    assertThat(message.body()).doesNotContain("\"eventType\"")
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
 
    assertThat(result?.userMessage).isEqualTo("Validation failed for one or more fields.")
    assertThat(result?.developerMessage).contains("bookingId must be supplied")
    assertThat(result?.developerMessage).contains("licenceId must be supplied")
    assertThat(result?.developerMessage).contains("nomsNumber must be supplied")
    assertThat(result?.developerMessage).contains("triggeredBy must be supplied")
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
