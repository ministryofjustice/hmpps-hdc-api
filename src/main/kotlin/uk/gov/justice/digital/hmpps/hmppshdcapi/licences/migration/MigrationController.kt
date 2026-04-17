package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFromHdcToCvlRequest

@RestController
@RequestMapping("/licences/migrate/active")
@Tag(name = "Licence Migration", description = "Operations related to licence migration to CVL")
class MigrationController(
  private val migrationService: MigrationService,
) {

  @PostMapping("/{activeLicenceId}/to-cvl")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Migrate a single active licence to CVL",
    description = "Triggers migration of the supplied licence ID into CVL",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Licence migrated to CVL successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
      ),
      ApiResponse(
        responseCode = "404",
        description = "Licence not found",
      ),
    ],
  )
  fun migrateLicenceToCvl(
    @PathVariable activeLicenceId: Long,
  ): ResponseEntity<Void> {
    migrationService.migrateToCvl(activeLicenceId)
    return ResponseEntity.ok().build()
  }

  @GetMapping("/{activeLicenceId}/to-cvl/preview")
  @PreAuthorize("hasAnyRole('HDC_ADMIN')")
  @Operation(
    summary = "Preview migration of a single active licence to CVL",
    description = "Returns the request object that would be sent to CVL",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Licence migration DTO created successfully"),
      ApiResponse(responseCode = "400", description = "Invalid request"),
      ApiResponse(responseCode = "404", description = "Licence not found"),
    ],
  )
  fun previewMigrateLicenceToCvl(
    @PathVariable activeLicenceId: Long,
  ): ResponseEntity<MigrateFromHdcToCvlRequest> {
    val response = migrationService.buildMigrationRequest(activeLicenceId)
    return ResponseEntity.ok(response)
  }
}
