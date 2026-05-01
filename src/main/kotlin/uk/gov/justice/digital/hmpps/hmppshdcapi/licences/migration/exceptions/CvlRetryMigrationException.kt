package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions

class CvlRetryMigrationException(
  val bookingId: Long?,
  val status: Int,
  override val message: String?,
) : RuntimeException(message)
