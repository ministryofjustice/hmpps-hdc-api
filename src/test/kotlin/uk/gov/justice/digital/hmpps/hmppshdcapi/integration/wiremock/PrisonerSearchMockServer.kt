package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner

class PrisonerSearchMockServer : WireMockServer(8099) {
  fun stubSearchPrisonersByBookingIds(prisoners: List<Prisoner>) {
    stubFor(
      post(urlEqualTo("/api/prisoner-search/booking-ids"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            ObjectMapper().writeValueAsString(prisoners),
          ).withStatus(200),
        ),
    )
  }
}
