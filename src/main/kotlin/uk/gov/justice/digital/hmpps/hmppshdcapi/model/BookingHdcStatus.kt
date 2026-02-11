package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatus

data class BookingHdcStatus(
  val bookingId: Long,
  val status: HdcStatus,
)
