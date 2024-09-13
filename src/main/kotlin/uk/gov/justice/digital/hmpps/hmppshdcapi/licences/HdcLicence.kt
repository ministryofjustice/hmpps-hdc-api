package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

enum class Decision {
  @JsonProperty("Yes")
  YES,

  @JsonProperty("No")
  NO,
}

enum class OfferAccepted {
  @JsonProperty("Yes")
  YES,

  @JsonProperty("Unavailable")
  UNAVAILABLE,

  @JsonProperty("Unsuitable")
  UNSUITABLE,
}

interface Address {
  val addressLine1: String
  val addressLine2: String?
  val addressTown: String
  val postCode: String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class LicenceData(
  val eligibility: Eligibility?,
  val bassReferral: Cas2Referral?,
  val proposedAddress: ProposedAddress?,
  val curfew: Curfew?,
  val risk: Risk?,
  val reporting: Reporting?,
  val victim: Victim?,
  val licenceConditions: LicenceConditions?,
  val documented: Document?,
  val approval: Approval?,
  val finalChecks: FinalChecks?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Eligibility(
  val crdTime: Decision,
  val excluded: Decision,
  val suitability: Decision
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Referral(
  // bassOffer only nullable as address will be either this if Cas2Referral or curfewAddress if proposed address
  val bassOffer: Cas2Offer? = null,
  val bassRequest: Cas2Request,
  val approvedPremisesAddress: CurfewAddress? = null,
  val bassAreaCheck: Cas2AreaCheck? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: CurfewAddress? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurfewAddress(
  override val addressLine1: String,
  override val addressLine2: String? = null,
  override val addressTown: String,
  override val postCode: String,
) : Address

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Offer(
  override val addressLine1: String,
  override val addressLine2: String? = null,
  override val addressTown: String,
  override val postCode: String,
  val bassAccepted: OfferAccepted,
) : Address

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Request(
  val bassRequested: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Curfew(
  val firstNight: FirstNight,
  val curfewHours: CurfewHours,
  val approvedPremisesAddress: CurfewAddress? = null,
  val approvedPremises: ApprovedPremises? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FirstNight(
  val firstNightFrom: String,
  val firstNightUntil: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurfewHours(
  val mondayFrom: String,
  val mondayUntil: String,
  val tuesdayFrom: String,
  val tuesdayUntil: String,
  val wednesdayFrom: String,
  val wednesdayUntil: String,
  val thursdayFrom: String,
  val thursdayUntil: String,
  val fridayFrom: String,
  val fridayUntil: String,
  val saturdayFrom: String,
  val saturdayUntil: String,
  val sundayFrom: String,
  val sundayUntil: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApprovedPremises(
  val required: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2AreaCheck(
  val approvedPremisesRequiredYesNo: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Risk (
  val riskManagement: RiskManagement
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RiskManagement(
  val version: String,
  val emsInformation: Decision?,
  val unsuitableReason: String?,
  val hasConsideredChecks: Decision?,
  val emsInformationDetails: String?,
  val riskManagementDetails: String?,
  val proposedAddressSuitable: Decision?,
  val awaitingOtherInformation: Decision?,
  val nonDisclosableInformation: Decision?,
  val nonDisclosableInformationDetails: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Victim (
  val victimLiaison: VictimLiaison
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VictimLiaison (
  val victimLiaison: Decision?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Approval (
  val release: Release
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Release (
  val decision: Decision?,
  val decisionMaker: String?,
  val reasonForDecision: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document (
  val template: Template
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Template (
  val decision: String?,
  val offenceCommittedBeforeFeb2015: Decision?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Reporting (
  val reportingInstructions: ReportingInstructions
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReportingInstructions (
  val name: String?,
  val postcode: String?,
  val telephone: String?,
  val townOrCity: String?,
  val organisation: String?,
  val reportingDate: String?,
  val reportingTime: String?,
  val buildingAndStreet1: String?,
  val buildingAndStreet2: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FinalChecks(
  val onRemand: Decision,
  val seriousOffence: Decision,
  val confiscationOrder: Decision
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LicenceConditions(
  val bespoke: List<String>?,
  val standard: Standard?,
  val additional: Additional?,
  val conditionsSummary: ConditionsSummary?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Standard(
  val additionalConditionsJustification: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Additional(
  // TO DO
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionsSummary(
  val additionalConditionsJustification: String?
)