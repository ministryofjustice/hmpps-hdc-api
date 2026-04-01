package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migrate.exceptions

class CvlMigrationException(
  val bookingId: Long?,
  val status: Int,
  val responseBody: String?,
) : RuntimeException(
  "Failed to migrate licence $bookingId to CVL (status=$status, body=$responseBody)",
)
