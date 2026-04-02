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
  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val homeDetentionCurfewEligibilityDate: LocalDate?,
  val pncNumber: String? = null,
  val status: String? = null,
  val mostSeriousOffence: String? = null,
  @field:JsonFormat(pattern = "yyyy-MM-dd")
  var homeDetentionCurfewActualDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  var homeDetentionCurfewEndDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val releaseDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val confirmedReleaseDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val conditionalReleaseDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val paroleEligibilityDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val actualParoleDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  var releaseOnTemporaryLicenceDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val postRecallReleaseDate: LocalDate? = null,

  val legalStatus: String? = null,

  val indeterminateSentence: Boolean? = null,

  val imprisonmentStatus: String? = null,

  val imprisonmentStatusDescription: String? = null,

  val recall: Boolean? = null,

  val locationDescription: String? = null,

  val prisonName: String? = null,

  val bookNumber: String? = null,

  val firstName: String? = null,

  val middleNames: String? = null,

  val lastName: String? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val conditionalReleaseDateOverrideDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val sentenceStartDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val sentenceExpiryDate: LocalDate? = null,

  @field:JsonFormat(pattern = "yyyy-MM-dd")
  val topupSupervisionStartDate: LocalDate? = null,

  val croNumber: String? = null,
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
