package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class CvlApiMockServer(port: Int = 8092) : WireMockServer(port) {

  fun stubMigrateLicenceSuccess() {
    stubFor(
      post(urlEqualTo("/licences/migrate"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200),
        ),
    )
  }

  fun stubMigrateLicenceFailure(status: Int, body: String? = null) {
    stubFor(
      post(urlEqualTo("/licences/migrate"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(body ?: "")
            .withStatus(status),
        ),
    )
  }
}
