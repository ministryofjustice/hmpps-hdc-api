package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.SCHEME_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.request.HdcCvlEventRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.response.HdcCvlEventResponse

@RestController
@RequestMapping("/licences/cvl-events", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
class HdcCvlEventController(
  private val hdcCvlEventPublisher: HdcCvlEventPublisher,
) {
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(
    summary = "Queue an HDC to CVL event",
    description = "Receives an HDC action request and publishes an event to the HDC to CVL queue. Requires ROLE_$ROLE_HDC_ADMIN.",
    security = [SecurityRequirement(name = SCHEME_HDC_ADMIN)],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "202",
        description = "Event accepted and queued",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = HdcCvlEventResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Validation failure",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun createEvent(
    @Valid @RequestBody request: HdcCvlEventRequest,
  ): HdcCvlEventResponse {
    val event = hdcCvlEventPublisher.publish(request)
    return HdcCvlEventResponse(
      eventType = requireNotNull(request.eventType).name,
      version = event.version,
      occurredAt = event.occurredAt,
    )
  }
}
