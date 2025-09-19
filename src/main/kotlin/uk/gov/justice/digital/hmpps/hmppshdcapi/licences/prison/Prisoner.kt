package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Prisoner(
  val prisonerNumber: String,
  val bookingId: String,
  val prisonId: String?,
  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val topupSupervisionExpiryDate: LocalDate?,
  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val licenceExpiryDate: LocalDate?,
)

data class Booking(
  val offenderNo: String,
  val bookingId: Long,
  val agencyId: String,
  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val topupSupervisionExpiryDate: LocalDate?,
  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val licenceExpiryDate: LocalDate?,
)
