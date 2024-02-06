package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

data class Prisoner(
  val prisonerNumber: String,
  val bookingId: String,
  val prisonId: String?,
)

data class Booking(
  val offenderNo: String,
  val bookingId: Long,
  val agencyId: String,
)
