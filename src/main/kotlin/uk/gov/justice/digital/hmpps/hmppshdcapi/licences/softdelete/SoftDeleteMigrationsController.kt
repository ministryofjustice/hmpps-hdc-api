package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.SCHEME_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService.MigrationBatchResponse

@RestController
@PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
@RequestMapping("/migrations", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoftDeleteMigrationsController(
  private val softDeleteService: SoftDeleteService,
) {
  @PostMapping("/delete-inactive-licences/{lastIdProcessed}/{batchSize}")
  @ResponseBody
  @Operation(
    summary = "Migration to populate the licences table with deleted at timestamp for soft deletion",
    description = "Migration to populate the licences table with deleted at timestamp for soft deletion." +
      "Requires $ROLE_HDC_ADMIN.",
    security = [SecurityRequirement(name = SCHEME_HDC_ADMIN)],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Migration response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = MigrationBatchResponse::class),
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
  fun populateDeletedAtForLicences(
    @PathVariable(name = "lastIdProcessed")
    @Parameter(name = "lastIdProcessed", description = "This is the Id number of the last licence to be processed in the previous batch migrated")
    @Min(0)
    lastIdProcessed: Long,
    @PathVariable(name = "batchSize")
    @Parameter(name = "batchSize", description = "This is the number of licences to migrate in one batch")
    @Min(1)
    @Max(1000)
    batchSize: Int,
  ) = softDeleteService.runMigration(lastIdProcessed, batchSize)
}
