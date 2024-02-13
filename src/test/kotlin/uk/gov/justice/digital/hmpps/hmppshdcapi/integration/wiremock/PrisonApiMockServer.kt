package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking

class PrisonApiMockServer : WireMockServer(8091) {
  fun stubGetByBookingId(booking: Booking) {
    stubFor(
      get(urlEqualTo("/api/bookings/${booking.bookingId}"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            ObjectMapper().writeValueAsString(booking),
          ).withStatus(200),
        ),
    )
  }
}
