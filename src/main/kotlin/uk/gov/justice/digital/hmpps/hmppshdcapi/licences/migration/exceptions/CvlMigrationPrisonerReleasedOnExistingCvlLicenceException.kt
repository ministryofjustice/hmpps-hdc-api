package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions

class CvlMigrationPrisonerReleasedOnExistingCvlLicenceException(
  val bookingId: Long,
  override val message: String?,
) : RuntimeException(message)
