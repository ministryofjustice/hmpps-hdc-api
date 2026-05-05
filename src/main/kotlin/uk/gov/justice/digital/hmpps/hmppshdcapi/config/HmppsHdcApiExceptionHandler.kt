package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.CvlRetryMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException

@RestControllerAdvice
class HmppsHdcApiExceptionHandler {

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.info("Access denied exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(
        ErrorResponse(
          status = HttpStatus.FORBIDDEN.value(),
          userMessage = "Authentication problem. Check token and roles - ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message, e) }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleValidationException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("No resource found exception: {}", e.message) }

  @ExceptionHandler(HandlerMethodValidationException::class)
  fun handleHandlerMethodValidationException(e: HandlerMethodValidationException): ResponseEntity<ErrorResponse> = e.allErrors.map { it.toString() }.distinct().sorted().joinToString("\n").let { validationErrors ->
    ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure(s): ${
            e.allErrors.map { it.defaultMessage }.distinct().sorted().joinToString("\n")
          }",
          developerMessage = "${e.message} $validationErrors",
        ),
      ).also { log.info("Validation exception: $validationErrors\n {}", e.message) }
  }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Unexpected error: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(
    CvlMigrationException::class,
    MigrationValidationException::class,
  )
  fun handleMigrationException(migrationException: Exception): ResponseEntity<ErrorResponse> {
    log.warn("Migration failed : {}", migrationException.message)

    val status = when (migrationException) {
      is MigrationValidationException -> BAD_REQUEST
      else -> UNPROCESSABLE_CONTENT
    }

    return ResponseEntity
      .status(status)
      .body(
        ErrorResponse(
          status = status.value(),
          userMessage = "Migration failed - ${migrationException.message}",
          developerMessage = migrationException.message,
        ),
      )
  }

  @ExceptionHandler(CvlRetryMigrationException::class)
  fun handleMigrationException(
    migrationException: CvlRetryMigrationException,
  ): ResponseEntity<ErrorResponse> {
    log.warn("Migration failed: {}", migrationException.message)

    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse(
          status = HttpStatus.CONFLICT.value(),
          userMessage = "Retryable migration failed - ${migrationException.message}",
          developerMessage = migrationException.message,
        ),
      )
  }

  @ExceptionHandler(NoDataFoundException::class)
  fun handleNoDataFoundException(e: NoDataFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Data not found: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Data not found exception: {}", e.message) }

  class NoDataFoundException(dataType: String, idType: String, id: Long) : Exception("No $dataType found for $idType $id")

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
