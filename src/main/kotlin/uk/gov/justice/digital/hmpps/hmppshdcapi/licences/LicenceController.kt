package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.SCHEME_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.BookingHdcStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

private const val HTTP_STATUS_NOT_SUPPORTED = 209

@RestController
@RequestMapping("/licence", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
class LicenceController(private val licenceService: LicenceService) {
  @GetMapping(value = ["/hdc/{bookingId}"])
  @ResponseBody
  @Operation(
    summary = "Get a licence matching the booking ID.",
    description = "Returns a licence for the booking ID." +
      "Requires ROLE_$ROLE_HDC_ADMIN.",
    security = [SecurityRequirement(name = SCHEME_HDC_ADMIN)],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Licence found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = HdcLicence::class),
          ),
        ],
      ),

      ApiResponse(
        responseCode = "204",
        description = "Licence for this booking ID was not found.",
      ),

      ApiResponse(
        responseCode = "$HTTP_STATUS_NOT_SUPPORTED",
        description = "Search by crn is not supported.",
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
      ApiResponse(
        responseCode = "404",
        description = "The licence data for this booking ID was not found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error occurred",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getHdcLicenceByBookingId(
    @PathVariable("bookingId") bookingId: Long,
  ) = licenceService.getByBookingId(bookingId)

  @PostMapping(value = ["/hdc/status"], consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseBody
  @Operation(
    summary = "Bulk HDC status lookup",
    description = "Returns the current HDC status for each booking ID where licence data exists." +
      "Requires ROLE_$ROLE_HDC_ADMIN.",
    security = [SecurityRequirement(name = SCHEME_HDC_ADMIN)],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns a list of HDC statuses",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = BookingHdcStatus::class),
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
  fun getHdcStatuses(
    @Parameter(required = true) @Valid @RequestBody bookingIds: List<Long>,
  ) = licenceService.getHdcStatuses(bookingIds)
}
