package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatus

data class BookingHdcStatus(
  @field:Schema(description = "The prison internal booking ID for the person on this licence", example = "989898")
  val bookingId: Long,

  @field:Schema(description = "The current HDC status code for this licence", example = "NOT_STARTED")
  val status: HdcStatus,
)
