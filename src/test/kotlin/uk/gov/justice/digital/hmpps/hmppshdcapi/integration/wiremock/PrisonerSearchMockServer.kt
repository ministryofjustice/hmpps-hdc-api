package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.LocalDate

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

  fun stubSearchPrisonersByBookingIds(bookingIds: List<Long>) {
    val today = LocalDate.now()

    val jsonArray = bookingIds.joinToString(
      prefix = "[",
      postfix = "]",
      separator = ",",
    ) { bookingId ->
      """
        {
          "prisonerNumber": "A1234AA",
          "bookingId": $bookingId,
          "prisonId": "MDI",
          "topupSupervisionExpiryDate": "$today", 
          "licenceExpiryDate": "${today.minusDays(1)}",
          "homeDetentionCurfewEligibilityDate": "${today.minusDays(2)}"
        }
      """.trimIndent()
    }

    stubFor(
      post(urlEqualTo("/api/prisoner-search/booking-ids"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(jsonArray)
            .withStatus(200),
        ),
    )
  }
}
