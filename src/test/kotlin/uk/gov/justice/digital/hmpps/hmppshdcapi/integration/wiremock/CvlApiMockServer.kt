package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.stubbing.Scenario
import org.springframework.http.HttpStatus

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
    stubMigrateLicenceClientError("/licences/migrate/active", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Service has failed")
  }

  fun stubMigrateLicenceWhenPrisonerIsReleasedOnCvlLicenceError() {
    val message = "HDC Licence should not be used, the prisoner has already been release on a CVL Licence!"
    stubMigrateLicenceClientError(
      "/licences/migrate/active",
      HttpStatus.CONFLICT,
      "NoRetryMigration error: $message",
      developerMessage = message,
      moreInfo = "PRISONER_RELEASED_ON_EXISTING_CVL_LICENCE",
    )
  }

  fun stubMigrateLicenceClientError(url: String, status: HttpStatus, message: String? = "userMessage", developerMessage: String? = "developerMessage", moreInfo: String? = "moreInfo") {
    stubFor(
      post(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(
              """
            {
              "status": ${status.value()},
              "userMessage": "$message",
              "developerMessage": "$developerMessage",
              "moreInfo": "$moreInfo"
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
