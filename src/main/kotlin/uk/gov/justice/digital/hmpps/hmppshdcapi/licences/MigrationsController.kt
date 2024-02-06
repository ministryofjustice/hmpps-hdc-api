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

const val MIGRATION_ROLE = "HDC_ADMIN"

@RestController
@PreAuthorize("hasAnyRole('$MIGRATION_ROLE')")
@RequestMapping("/migrations", produces = [MediaType.APPLICATION_JSON_VALUE])
class MigrationsController(
  private val populateLicencePrisonNumberMigration: PopulateLicencePrisonNumberMigration,
  private val populateLicenceVersionPrisonNumberMigration: PopulateLicenceVersionPrisonNumberMigration,
) {

  @PostMapping("/populate-prison-numbers-for-licences/{numberToMigrate}")
  @ResponseBody
  @Operation(
    summary = "Migration job to populate the licences table with prison numbers",
    description = "Migration job to populate the licences table with prison numbers. " +
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
            schema = Schema(implementation = PopulateLicencePrisonNumberMigration.Response::class),
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
  fun populatePrisonNumbersForLicences(
    @PathVariable(name = "numberToMigrate")
    @Parameter(name = "numberToMigrate", description = "This is the number of licences to migrate in one batch")
    @Min(1)
    numberToMigrate: Int,
  ) = populateLicencePrisonNumberMigration.run(numberToMigrate)

  @PostMapping("/populate-prison-numbers-for-licence-versions/{numberToMigrate}")
  @ResponseBody
  @Operation(
    summary = "Migration job to populate the licence versions table with prison numbers",
    description = "Migration job to populate the licence versions table with prison numbers. " +
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
            schema = Schema(implementation = PopulateLicenceVersionPrisonNumberMigration.Response::class),
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
  fun populatePrisonNumbersForLicenceVersions(
    @PathVariable(name = "numberToMigrate")
    @Parameter(
      name = "numberToMigrate",
      description = "This is the number of licence versions to migrate in one batch",
    )
    @Min(1)
    numberToMigrate: Int,
  ) = populateLicenceVersionPrisonNumberMigration.run(numberToMigrate)
}
