package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class Prisoner(
  val prisonerNumber: String,
  val bookingId: String,
  val prisonId: String?,
)

data class Booking(
  val offenderNo: String,
  val bookingId: Long,
  val agencyId: String,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val topupSupervisionExpiryDate: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val licenceExpiryDate: LocalDateTime?,
)
