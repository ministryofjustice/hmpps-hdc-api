package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN

@Tag(name = "Jobs")
@RestController
class MigrationJobController(
  private val migrationProcessService: MigrationProcessService,
) {

  @PostMapping("/jobs/licences-migrate-batch-to-cvl")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Migrate a batch of licences to CVL on a schedule",
    description = "Triggers migration of licences into CVL on a schedule",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "202",
        description = "Migration schedule started successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
      ),
    ],
  )
  fun migrateABatchOfLicences(): ResponseEntity<String> {
    migrationProcessService.migrateABatchOfLicences()
    return ResponseEntity.accepted().body("Migration schedule started")
  }
}
