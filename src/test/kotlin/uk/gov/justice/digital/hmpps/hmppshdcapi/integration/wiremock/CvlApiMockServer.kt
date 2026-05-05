package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.stubbing.Scenario

class CvlApiMockServer(port: Int = 8092) : WireMockServer(port) {

  fun stubMigrateLicenceClient400Error() {
    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(400)
            .withBody(
              """
            {
              "status": 400,
              "userMessage": "Invalid id",
              "developerMessage": "it does not exist"
            }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubMigrateLicenceClient500Error() {
    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(500)
            .withBody(
              """
            {
              "status": 500,
              "userMessage": "Internal server error",
              "developerMessage": "Service has failed"
            }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubMigrateLicenceClient500Then200Then405() {
    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .inScenario("migrate-licence-retry")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(500)
            .withBody(
              """
            {
              "status": 500,
              "userMessage": "Internal server error",
              "developerMessage": "Service has failed - retry"
            }
              """.trimIndent(),
            ),
        )
        .willSetStateTo("SECOND_CALL"),
    )

    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .inScenario("migrate-licence-retry")
        .whenScenarioStateIs("SECOND_CALL")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody("""{ "status": "OK" }"""),
        )
        .willSetStateTo("THIRD_CALL"),
    )

    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .inScenario("migrate-licence-retry")
        .whenScenarioStateIs("THIRD_CALL")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(405)
            .withBody(
              """
            {
              "status": 405,
              "userMessage": "Method not allowed",
              "developerMessage": "Final failure - dont retry"
            }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubMigrateLicenceSuccess() {
    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200),
        ),
    )
  }

  fun stubMigrateLicenceFailure(status: Int, body: String? = null) {
    stubFor(
      post(urlEqualTo("/licences/migrate/active"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(body ?: "")
            .withStatus(status),
        ),
    )
  }
}
