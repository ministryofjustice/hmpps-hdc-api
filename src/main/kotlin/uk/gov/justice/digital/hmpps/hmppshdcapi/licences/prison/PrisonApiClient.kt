package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.typeReference
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.Batching.batchRequests

private const val HDC_BATCH_SIZE = 500

@Service
class PrisonApiClient(@param:Qualifier("oauthPrisonClient") val prisonApiWebClient: WebClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun resetHdcChecks(bookingId: Long): Boolean {
    val response = prisonApiWebClient
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

  fun getHdcStatus(bookingId: Long): PrisonerHdcStatus? = prisonApiWebClient
    .get()
    .uri("/offender-sentences/booking/{bookingId}/home-detention-curfews/latest", bookingId)
    .accept(MediaType.APPLICATION_JSON)
    .retrieve()
    .bodyToMono(PrisonerHdcStatus::class.java)
    .block()

  fun getHdcStatuses(bookingIds: List<Long>, batchSize: Int = HDC_BATCH_SIZE) = batchRequests(batchSize, bookingIds) { batch ->
    prisonApiWebClient
      .post()
      .uri("/offender-sentences/home-detention-curfews/latest")
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(batch)
      .retrieve()
      .bodyToMono(typeReference<List<PrisonerHdcStatus>>())
      .block()
  }
}
