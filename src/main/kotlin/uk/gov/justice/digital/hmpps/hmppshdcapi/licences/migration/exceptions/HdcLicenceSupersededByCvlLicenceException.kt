package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions

class HdcLicenceSupersededByCvlLicenceException(
  val bookingId: Long,
  override val message: String?,
) : RuntimeException(message)
