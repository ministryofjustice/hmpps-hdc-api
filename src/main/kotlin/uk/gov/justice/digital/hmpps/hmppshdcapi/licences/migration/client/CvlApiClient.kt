package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlRetryMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFromHdcToCvlRequest
@Service
class CvlApiClient(
  @param:Qualifier("oauthCvlClient") val cvlWebClient: WebClient,
) {
  private val mapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()

  fun migrateLicence(request: MigrateFromHdcToCvlRequest) {
    cvlWebClient
      .post()
      .uri("/licences/migrate/active")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(request)
      .retrieve()
      .onStatus({ it.isError }) { response ->
        response.bodyToMono(ByteArray::class.java)
          .defaultIfEmpty(ByteArray(0))
          .map { bodyBytes ->
            handleMigrationErrorResponse(
              status = response.statusCode(),
              bodyBytes = bodyBytes,
              bookingId = request.bookingId,
            )
          }
      }
      .toBodilessEntity()
      .block()

    log.info("Successfully migrated licence ${request.bookingId} to CVL")
  }

  private fun handleMigrationErrorResponse(
    status: HttpStatusCode,
    bodyBytes: ByteArray,
    bookingId: Long,
  ): Nothing {
    val body = runCatching {
      mapper.readValue(bodyBytes, ErrorResponse::class.java)
    }.getOrNull()

    log.warn(
      "Failed to migrate licence $bookingId to CVL, " +
        "status:$status, uri:/licences/migrate/active, body:$body",
    )

    val message = body?.developerMessage

    throw if (status.value() in RETRYABLE_STATUS_VALUES) {
      CvlRetryMigrationException(
        bookingId = bookingId,
        status = status.value(),
        message = message,
      )
    } else {
      CvlMigrationException(
        bookingId = bookingId,
        status = status.value(),
        message = message,
      )
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    val CF_WEB_SERVER_IS_DOWN: HttpStatusCode? = HttpStatusCode.valueOf(521)
    val CF_CONNECTION_TIMED_OUT: HttpStatusCode = HttpStatusCode.valueOf(522)
    val CF_TIMEOUT_OCCURRED: HttpStatusCode = HttpStatusCode.valueOf(524)

    private val RETRYABLE_STATUS = setOf(
      HttpStatus.REQUEST_TIMEOUT, // 408
      HttpStatus.PAYLOAD_TOO_LARGE, // 413
      HttpStatus.TOO_MANY_REQUESTS, // 429
      HttpStatus.INTERNAL_SERVER_ERROR, // 500
      HttpStatus.BAD_GATEWAY, // 502
      HttpStatus.SERVICE_UNAVAILABLE, // 503
      HttpStatus.GATEWAY_TIMEOUT, // 504
      CF_WEB_SERVER_IS_DOWN,
      CF_CONNECTION_TIMED_OUT,
      CF_TIMEOUT_OCCURRED,
    )
    private val RETRYABLE_STATUS_VALUES = RETRYABLE_STATUS.map { it!!.value() }.toSet()
  }
}
