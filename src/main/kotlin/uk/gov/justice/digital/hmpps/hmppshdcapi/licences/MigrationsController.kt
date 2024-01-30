package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse

const val MIGRATION_ROLE = "HDC_MIGRATION_ADMIN"

@RestController
@PreAuthorize("hasAnyRole('$MIGRATION_ROLE')")
@RequestMapping("/migrations", produces = [MediaType.APPLICATION_JSON_VALUE])
class MigrationsController(private val populatePrisonNumberMigration: PopulatePrisonNumberMigration) {

  @PostMapping("/populate-prison-numbers/{numberToMigrate}")
  @ResponseBody
  @Operation(
    summary = "Migration job to populate licences table with prison numbers",
    description = "Migration job to populate licences table with prison numbers. " +
      "Requires $MIGRATION_ROLE.",
    security = [SecurityRequirement(name = "ROLE_$MIGRATION_ROLE")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Migration response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PopulatePrisonNumberMigration.Response::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun populatePrisonNumbers(
    @PathVariable(name = "numberToMigrate")
    @Parameter(name = "numberToMigrate", description = "This is the number of licences to migrate in one batch")
    @Min(1)
    numberToMigrate: Int,
  ) = populatePrisonNumberMigration.run(numberToMigrate)
}
