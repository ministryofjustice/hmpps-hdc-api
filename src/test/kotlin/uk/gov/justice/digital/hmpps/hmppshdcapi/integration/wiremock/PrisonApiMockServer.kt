package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking

class PrisonApiMockServer : WireMockServer(8091) {
  private val mapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()
  fun stubGetByBookingId(booking: Booking) {
    stubFor(
      get(urlPathEqualTo("/api/bookings/${booking.bookingId}"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            mapper.writeValueAsString(booking),
          ).withStatus(200),
        ),
    )
  }
  fun resetHdc(bookingId: Long, status: HttpStatusCode) {
    stubFor(
      delete(urlPathEqualTo("/api/offender-sentences/booking/$bookingId/home-detention-curfews/latest/checks-passed"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withStatus(status.value()),
        ),
    )
  }

  fun checkHdcResetCalled(bookingId: Long) {
    verify(1, deleteRequestedFor(urlEqualTo("/api/offender-sentences/booking/$bookingId/home-detention-curfews/latest/checks-passed")))
  }

  fun getHdcStatuses(statuses: List<Pair<Long, String>>) {
    val jsonArray = statuses.joinToString(
      prefix = "[",
      postfix = "]",
      separator = ",",
    ) { (bookingId, approvalStatus) ->
      """
      {
        "bookingId": "$bookingId",
        "passed": true,
        "approvalStatus": "$approvalStatus"
      }
      """.trimIndent()
    }

    stubFor(
      post(urlEqualTo("/api/offender-sentences/home-detention-curfews/latest"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(jsonArray)
            .withStatus(200),
        ),
    )
  }
}
