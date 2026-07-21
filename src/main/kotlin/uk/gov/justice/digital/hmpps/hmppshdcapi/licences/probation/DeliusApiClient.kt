package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.probation

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.typeReference
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.probation.CommunityManager

@Component
class DeliusApiClient(@param:Qualifier("oauthDeliusApiClient") val deliusApiWebClient: WebClient) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getOffenderManager(crnOrNomisId: String): CommunityManager? = deliusApiWebClient
    .get()
    .uri("/probation-case/{crnOrNomisId}/responsible-community-manager", crnOrNomisId)
    .accept(MediaType.APPLICATION_JSON)
    .retrieve()
    .bodyToMono(typeReference<CommunityManager>())
    .coerce404ToEmptyOrThrow()
    .block()


  fun <T : Any> Mono<T>.coerce404ToEmptyOrThrow() = this.onErrorResume {
    when {
      it is WebClientResponseException && it.statusCode == HttpStatus.NOT_FOUND -> {
        val uri = it.request?.uri?.toString() ?: "Unknown"
        log.info("No resource found for URI: $uri")
        Mono.empty()
      }

      else -> Mono.error(it)
    }
  }
}

