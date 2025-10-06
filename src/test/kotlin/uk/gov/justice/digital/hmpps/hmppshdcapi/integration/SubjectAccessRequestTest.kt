package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode.STRICT
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_SAR_DATA_ACCESS
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import java.nio.charset.StandardCharsets.UTF_8

class SubjectAccessRequestTest : SqsIntegrationTestBase() {

  private fun jsonFromFile(name: String) = this.javaClass.getResourceAsStream("/test_data/responses/$name")!!.bufferedReader(UTF_8).readText()

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_$ROLE_HDC_ADMIN", "ROLE_$ROLE_SAR_DATA_ACCESS"])
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/subject-access-request.sql",
  )
  fun `Check response of subject access request`(role: String) {
    webTestClient.get()
      .uri("/subject-access-request?prn=A1234AA")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus().isOk()
      .expectBody().json(jsonFromFile("subject-access-request.json"), STRICT)
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_$ROLE_HDC_ADMIN", "ROLE_$ROLE_SAR_DATA_ACCESS"])
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/subject-access-request-with-conditions-v1.sql",
  )
  fun `Check response of subject access request with V1 of the additional conditions`(role: String) {
    webTestClient.get()
      .uri("/subject-access-request?prn=A1234AA")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus().isOk()
      .expectBody().json(jsonFromFile("subject-access-request-with-conditions-v1.json"), STRICT)
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_$ROLE_HDC_ADMIN", "ROLE_$ROLE_SAR_DATA_ACCESS"])
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/subject-access-request-with-conditions-v2.sql",
  )
  fun `Check response of subject access request with V2 of the additional conditions`(role: String) {
    webTestClient.get()
      .uri("/subject-access-request?prn=A1234AA")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus().isOk()
      .expectBody().json(jsonFromFile("subject-access-request-with-conditions-v2.json"), STRICT)
  }

  @ParameterizedTest
  @ValueSource(strings = ["ROLE_$ROLE_HDC_ADMIN", "ROLE_$ROLE_SAR_DATA_ACCESS"])
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/subject-access-request.sql",
  )
  fun `Check response of subject access request when no result`(role: String) {
    webTestClient.get()
      .uri("/subject-access-request?prn=ZZZZZZ")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf(role)))
      .exchange()
      .expectStatus().isNoContent()
  }
}
