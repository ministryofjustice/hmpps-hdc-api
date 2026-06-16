package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFromHdcToCvlRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.response.LicenceMigrationLogEntryDto

@RestController
@RequestMapping("/licences/migrate")
@Tag(name = "Licence Migration", description = "Operations related to licence migration to CVL")
class MigrationController(
  private val migrationProcessService: MigrationProcessService,
  private val migrationRequestService: MigrationRequestService,
) {

  @PostMapping("/{bookingId}/to-cvl")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Migrate a single active licence to CVL",
    description = "Triggers migration of licence for the supplied booking ID into CVL",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Licence migrated to CVL successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Migration validation failed",
      ),
      ApiResponse(
        responseCode = "422",
        description = "Migration rejected due to business rules or CVL state",
      ),
      ApiResponse(
        responseCode = "503",
        description = "CVL temporarily unavailable, retryable migration failure",
      ),
    ],
  )
  fun migrateALicence(
    @PathVariable bookingId: Long,
  ): ResponseEntity<Void> {
    migrationProcessService.migrateALicence(bookingId)
    return ResponseEntity.noContent().build()
  }

  @PostMapping("/batch/to-cvl")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Migrate a batch of licences to CVL",
    description = "Triggers migration of licences into CVL",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "202",
        description = "Migration started successfully",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
      ),
    ],
  )
  fun migrateABatchOfLicences(): ResponseEntity<String> {
    migrationProcessService.migrateABatchOfLicences()
    return ResponseEntity.accepted().body("Migration started")
  }

  @GetMapping("/{activeLicenceId}/to-cvl/preview")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
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
    val response = migrationRequestService.buildMigrationRequestForPreview(activeLicenceId)
    return ResponseEntity.ok(response)
  }

  @GetMapping("/logs")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Returns recent licence migration logs",
    description = "Returns the entries from the licence_migration_log table",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Page of migration logs returned successfully"),
      ApiResponse(responseCode = "401", description = "Unauthorized"),
      ApiResponse(responseCode = "403", description = "Forbidden"),
    ],
  )
  fun getMigrationLogs(
    @RequestParam(required = false) licenceVersionId: Long?,
    @RequestParam(required = false) bookingId: Long?,
    @RequestParam(required = false) errorSource: String?,
    @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC, size = 100) pageable: Pageable,
  ): ResponseEntity<Page<LicenceMigrationLogEntryDto>> {
    val response = migrationProcessService.getMigrationLogs(licenceVersionId, bookingId, errorSource, pageable)
    return ResponseEntity.ok(response)
  }

  @PutMapping("/{licenceVersionId}/retry/{retryValue}")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Update the retry flag for a licence version",
    description = "Updates the retry flag in the migration log for the given licence version ID",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "204", description = "Retry flag updated successfully"),
      ApiResponse(responseCode = "401", description = "Unauthorized"),
      ApiResponse(responseCode = "403", description = "Forbidden"),
    ],
  )
  fun updateRetryState(
    @PathVariable licenceVersionId: Long,
    @PathVariable retryValue: Boolean,
  ): ResponseEntity<Void> {
    migrationProcessService.updateRetryState(licenceVersionId, retryValue)
    return ResponseEntity.noContent().build()
  }
}
