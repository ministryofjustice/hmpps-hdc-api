package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.migration

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase

class MigrationControllerTest : SqsIntegrationTestBase() {

  @Test
  fun `Migrate licence to CVL successfully`() {
    val licenceId = 12345L

    // Given
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = webTestClient.post()
      .uri("/licences/migration/$licenceId/to-cvl")
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()

    // Then
    response.expectStatus().isOk

    // Verify the request payload sent to CVL
    cvlMockServer.verify(
      1,
      postRequestedFor(urlEqualTo("/licences/migrate"))
        .withRequestBody(matchingJsonPath("$.bookingNo", equalTo("A1234BC")))
        .withRequestBody(matchingJsonPath("$.prisoner.prisonerNumber", equalTo("A1234BC")))
        .withRequestBody(matchingJsonPath("$.prison.prisonCode", equalTo("MDI")))
        .withRequestBody(matchingJsonPath("$.licence.typeCode", equalTo("AP"))),
    )
  }

  companion object {
    private val cvlMockServer = CvlApiMockServer()

    @JvmStatic
    @BeforeAll
    fun startWireMocks() {
      cvlMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopWireMocks() {
      cvlMockServer.stop()
    }
  }
}
