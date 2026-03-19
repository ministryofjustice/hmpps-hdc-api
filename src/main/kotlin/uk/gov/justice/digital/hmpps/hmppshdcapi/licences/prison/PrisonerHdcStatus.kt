package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

import com.fasterxml.jackson.annotation.JsonProperty

data class PrisonerHdcStatus(
  val approvalStatus: String? = null,
  val approvalStatusDate: String? = null,
  val bookingId: Long? = null,
  val checksPassedDate: String? = null,
  @field:JsonProperty(defaultValue = "false")
  val passed: Boolean,
  val refusedReason: String? = null,
) {
  fun isApproved() = approvalStatus == "APPROVED"
}
