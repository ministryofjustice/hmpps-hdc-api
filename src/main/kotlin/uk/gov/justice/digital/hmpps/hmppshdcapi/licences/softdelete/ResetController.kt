package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.SCHEME_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.ResetService.ResetResponse

@RestController
@PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
@RequestMapping("/licences", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResetController(
  private val resetService: ResetService,
) {
  @PostMapping("/reset")
  @ResponseBody
  @Operation(
    summary = "Allows for bulk reset of licences" +
      "Requires $ROLE_HDC_ADMIN.",
    security = [SecurityRequirement(name = SCHEME_HDC_ADMIN)],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Information about reset licences",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ResetResponse::class),
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
  fun resetLicences(
    @Parameter(required = true) @Valid @RequestBody bookingIds: List<Long>,
  ) = resetService.resetLicences(bookingIds)
}
