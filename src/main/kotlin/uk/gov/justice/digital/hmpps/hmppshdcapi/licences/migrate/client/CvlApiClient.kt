package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migrate.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migrate.exceptions.CvlMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migrate.request.MigrateFromHdcToCvlRequest

@Service
class CvlApiClient(
  @param:Qualifier("oauthCvlClient") val cvlWebClient: WebClient,
) {

  fun migrateLicence(request: MigrateFromHdcToCvlRequest) {
    val response = cvlWebClient
      .post()
      .uri("/licences/migrate")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(request)
      .exchangeToMono { Mono.just(it) }
      .block()!!

    if (response.statusCode().is2xxSuccessful) {
      log.info(
        "Successfully migrated licence ${request.bookingId} to CVL, status:${response.statusCode()}",
      )
    } else {
      val body = response.toEntity(String::class.java).block()?.body

      log.warn(
        "Failed to migrate licence ${request.bookingId} to CVL, status:${response.statusCode()}, uri: ${response.request().uri}, body: $body",
      )

      throw CvlMigrationException(
        bookingId = request.bookingId,
        status = response.statusCode().value(),
        responseBody = body,
      )
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
