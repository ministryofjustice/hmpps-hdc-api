package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ProtectedByIngress

@Tag(name = "Jobs")
@RestController
class MigrationJobController(
  private val migrationProcessService: MigrationProcessService,
) {

  @ProtectedByIngress
  @PostMapping("/jobs/licences-migrate-batch-to-cvl")
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun migrateABatchOfLicencesJob() {
    migrationProcessService.migrateABatchOfLicences()
  }
}
