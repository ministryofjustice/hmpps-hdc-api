package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner

class PrisonerSearchMockServer : WireMockServer(8099) {
  private val mapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()
  fun stubSearchPrisonersByBookingIds(prisoners: List<Prisoner>) {
    stubFor(
      post(urlEqualTo("/api/prisoner-search/booking-ids"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            mapper.writeValueAsString(prisoners),
          ).withStatus(200),
        ),
    )
  }
}
