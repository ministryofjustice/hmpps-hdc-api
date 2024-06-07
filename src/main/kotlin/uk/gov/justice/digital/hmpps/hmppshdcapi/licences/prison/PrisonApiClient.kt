package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatusCode
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

  fun resetHdcChecks(bookingId: Long): HttpStatusCode? = prisonerSearchApiWebClient
    .delete()
    .uri("/api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed", bookingId)
    .accept(MediaType.APPLICATION_JSON)
    .exchangeToMono { Mono.just(it.statusCode()) }
    .onErrorResume(::coerceClientErrorToStatusCode)
    .block()


  private fun coerceClientErrorToStatusCode(exception: Throwable): Mono<HttpStatusCode?> =
    with(exception) {
      when (this) {
        is WebClientResponseException -> {
          log.info("failed on call ${request?.uri?.path}", exception)
          Mono.just(this.statusCode)
        }

        else -> {
          log.info("failed call due to unexpected reason: ${exception.message}", exception)
          Mono.empty()
        }
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
