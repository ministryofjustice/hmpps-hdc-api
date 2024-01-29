package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.typeReference

@Service
class PrisonSearchApiClient(@Qualifier("oauthPrisonerSearchClient") val prisonerSearchApiWebClient: WebClient) {

  fun getPrisonersByBookingIds(bookingIds: Collection<Long>): List<Prisoner> {
    if (bookingIds.isEmpty()) return emptyList()
    return prisonerSearchApiWebClient
      .post()
      .uri("/prisoner-search/booking-ids")
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(PrisonerSearchByBookingIdsRequest(bookingIds.toList()))
      .retrieve()
      .bodyToMono(typeReference<List<Prisoner>>())
      .block() ?: emptyList()
  }

  data class PrisonerSearchByBookingIdsRequest(
    val bookingIds: List<Long>,
  )
}
