package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Licence migration log entry")
data class LicenceMigrationLogEntryDto(
  @get:Schema(description = "Log entry id", example = "123")
  val id: Long,
  @get:Schema(description = "Licence version id", example = "42")
  val licenceVersionId: Long? = null,
  @get:Schema(description = "Timestamp of the log entry", example = "2024-06-01T12:34:56")
  val createdTimeStamp: LocalDateTime,
  @get:Schema(description = "Booking id", example = "987654")
  val bookingId: Long,
  @get:Schema(description = "Was the migration successful")
  val success: Boolean,
  @get:Schema(description = "Should this failure be retried")
  val retry: Boolean,
  @get:Schema(description = "Message for the log entry")
  val message: String?,
  @get:Schema(description = "Error source if failed", allowableValues = ["CVL", "HDC"], example = "CVL")
  val errorSource: String?,
)
