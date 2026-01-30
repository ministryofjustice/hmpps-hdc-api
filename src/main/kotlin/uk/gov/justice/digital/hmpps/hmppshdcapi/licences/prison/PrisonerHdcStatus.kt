package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

data class PrisonerHdcStatus(
  val approvalStatus: String? = null,
  val approvalStatusDate: String? = null,
  val bookingId: Long? = null,
  val checksPassedDate: String? = null,
  val passed: Boolean,
  val refusedReason: String? = null,
) {
  fun isApproved() = approvalStatus == "APPROVED"
}
