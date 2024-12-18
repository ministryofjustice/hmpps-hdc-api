package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class PrisonApiClient(@Qualifier("oauthPrisonClient") val prisonerSearchApiWebClient: WebClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getBooking(bookingId: Long): Booking? {
    val booking = prisonerSearchApiWebClient
      .get()
      .uri("/bookings/{bookingId}", bookingId)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(Booking::class.java)
      .onErrorResume(::coerce404ResponseToNull)
      .block()

    return booking
  }

  fun resetHdcChecks(bookingId: Long): Boolean {
    val response = prisonerSearchApiWebClient
      .delete()
      .uri("/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed", bookingId)
      .accept(MediaType.APPLICATION_JSON)
      .exchangeToMono {
        Mono.just(it)
      }
      .block()!!

    return if (response.statusCode().is2xxSuccessful) {
      log.info("Successfully reset licence for booking $bookingId, status:${response.statusCode()}")
      true
    } else {
      log.warn(
        "Failed to reset licence for booking $bookingId, status:${response.statusCode()}, uri: ${response.request().uri}, body: ${
          response.toEntity(String::class.java).block()
        }",
      )
      false
    }
  }

  private fun <API_RESPONSE_BODY_TYPE> coerce404ResponseToNull(exception: Throwable): Mono<API_RESPONSE_BODY_TYPE> =
    with(exception) {
      when {
        this is WebClientResponseException && statusCode == NOT_FOUND -> {
          log.info("No resource found when calling prisoner-api ${request?.uri?.path}")
          Mono.empty()
        }

        else -> Mono.error(exception)
      }
    }
}
