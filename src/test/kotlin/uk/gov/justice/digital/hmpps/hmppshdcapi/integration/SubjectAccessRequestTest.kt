package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode.STRICT
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_SAR_DATA_ACCESS
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import java.nio.charset.StandardCharsets.UTF_8

class SubjectAccessRequestTest : SqsIntegrationTestBase() {

  private fun jsonFromFile(name: String) =
    this.javaClass.getResourceAsStream("/test_data/responses/$name")!!.bufferedReader(UTF_8).readText()

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/subject-access-request.sql",
  )
  fun `Check response of subject access request`() {
    webTestClient.get()
      .uri("/subject-access-request?prn=A1234AA")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_$ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isOk()
      .expectBody().json(jsonFromFile("subject-access-request.json"), STRICT)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/subject-access-request.sql",
  )
  fun `Check response of subject access request when no result`() {
    webTestClient.get()
      .uri("/subject-access-request?prn=ZZZZZZ")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_$ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isNoContent()
  }
}
