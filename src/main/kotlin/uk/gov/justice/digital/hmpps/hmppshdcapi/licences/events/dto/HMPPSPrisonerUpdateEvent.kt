package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto

data class HMPPSPrisonerUpdateEvent(
  val additionalInformation: HMPPSPrisonerUpdatedAdditionalInformation,
)

data class HMPPSPrisonerUpdatedAdditionalInformation(
  val nomsNumber: String,
  val reason: String? = null,
)
