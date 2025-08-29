package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalTime

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
  val addressLine1: String?
  val addressLine2: String?
  val addressTown: String?
  val postCode: String?
}

@JsonInclude(NON_NULL)
data class LicenceData(
  val eligibility: Eligibility?,
  val bassReferral: Cas2Referral?,
  val proposedAddress: ProposedAddress?,
  val curfew: Curfew?,
  val risk: Risk?,
  val reporting: Reporting?,
  val victim: Victim?,
  val licenceConditions: LicenceConditions?,
  val document: Document?,
  val approval: Approval?,
  val finalChecks: FinalChecks?,
)

@JsonInclude(NON_NULL)
data class Eligibility(
  val crdTime: DecisionMade?,
  val excluded: DecisionMade?,
  val suitability: DecisionMade?,
)

@JsonInclude(NON_NULL)
data class DecisionMade(
  val decision: Decision?,
)

@JsonInclude(NON_NULL)
data class Cas2Referral(
  // bassOffer only nullable as address will be either this if Cas2Referral or curfewAddress if proposed address
  val bassOffer: Cas2Offer? = null,
  val bassRequest: Cas2Request? = null,
  val approvedPremisesAddress: CurfewAddress? = null,
  val bassAreaCheck: Cas2AreaCheck? = null,
)

@JsonInclude(NON_NULL)
data class ProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: CurfewAddress? = null,
  val addressProposed: DecisionMade? = null,
  val optOut: DecisionMade? = null,
  val rejections: List<Rejection>? = null,
)

@JsonInclude(NON_NULL)
data class Rejection(
  val address: CurfewAddress?,
  val addressReview: AddressReviewWrapper? = null,
  val riskManagement: RiskManagement? = null,
  val withdrawalReason: String? = null,
)

@JsonInclude(NON_NULL)
data class AddressReviewWrapper(
  val curfewAddressReview: AddressReview? = null,
)

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "version",
  defaultImpl = AddressReviewV1::class,
)
sealed interface AddressReview

@JsonInclude(NON_NULL)
@JsonTypeName("1")
data class AddressReviewV1(
  val version: String? = null,
  val addressReviewComments: String? = null,
  val consent: String? = null,
  val electricity: String? = null,
  val homeVisitConducted: String? = null,
) : AddressReview

@JsonInclude(NON_NULL)
@JsonTypeName("2")
data class AddressReviewV2(
  val version: String? = null,
  val addressReviewComments: String? = null,
  val consentHavingSpoken: String? = null,
  val electricity: String? = null,
  val homeVisitConducted: String? = null,
) : AddressReview

@JsonInclude(NON_NULL)
data class Occupier(
  val name: String? = null,
  val relationship: String? = null,
  val isOffender: String? = null,
)

@JsonInclude(NON_NULL)
data class CurfewAddress(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
  val occupier: Occupier? = null,
  val residents: List<Resident>? = null,
  val telephone: String? = null,
  val additionalInformation: String? = null,
  val residentOffenceDetails: String? = null,
  val cautionedAgainstResident: Decision? = null,
) : Address

@JsonInclude(NON_NULL)
data class Resident(
  val age: String? = null,
  val name: String? = null,
  val relationship: String? = null,
)

@JsonInclude(NON_NULL)
data class Cas2Offer(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
  val bassAccepted: OfferAccepted?,
  val telephone: String? = null,
  val bassArea: String? = null,
  val bassOfferDetails: String? = null,
) : Address

@JsonInclude(NON_NULL)
data class Cas2Request(
  val bassRequested: Decision?,
  val specificArea: Decision? = null,
  val additionalInformation: String? = null,
  val proposedCounty: String? = null,
  val proposedTown: String? = null,
)

@JsonInclude(NON_NULL)
data class Curfew(
  val firstNight: FirstNight?,
  val curfewHours: CurfewHours?,
  val approvedPremisesAddress: CurfewAddress? = null,
  val approvedPremises: ApprovedPremises? = null,
  val curfewAddressReview: AddressReview? = null,
)

@JsonInclude(NON_NULL)
data class FirstNight(
  @field:JsonFormat(pattern = "HH:mm")
  val firstNightFrom: LocalTime,
  @field:JsonFormat(pattern = "HH:mm")
  val firstNightUntil: LocalTime,
)

@JsonInclude(NON_NULL)
data class CurfewHours(
  @field:JsonFormat(pattern = "HH:mm")
  val mondayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val mondayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val tuesdayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val tuesdayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val wednesdayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val wednesdayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val thursdayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val thursdayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val fridayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val fridayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val saturdayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val saturdayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val sundayFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val sundayUntil: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val allFrom: LocalTime?,
  @field:JsonFormat(pattern = "HH:mm")
  val allUntil: LocalTime?,
  val daySpecificInputs: Decision? = null,
)

@JsonInclude(NON_NULL)
data class ApprovedPremises(
  val required: Decision,
)

@JsonInclude(NON_NULL)
data class Cas2AreaCheck(
  val approvedPremisesRequiredYesNo: Decision,
  val bassAreaCheck: String? = null,
  val bassAreaCheckSeen: String? = null,
  val bassAreaReason: String? = null,
)

@JsonInclude(NON_NULL)
data class Risk(
  val riskManagement: RiskManagement,
)

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "version",
)
sealed interface RiskManagement

@JsonTypeName("1")
@JsonInclude(NON_NULL)
data class RiskManagementV1(
  val version: String? = "1",
  val awaitingInformation: String? = null,
  val emsInformation: String? = null,
  val emsInformationDetails: String? = null,
  val nonDisclosableInformation: String? = null,
  val nonDisclosableInformationDetails: String? = null,
  val planningActions: String? = null,
  val proposedAddressSuitable: String? = null,
  val riskManagementDetails: String? = null,
  val unsuitableReason: String? = null,
) : RiskManagement

@JsonTypeName("2")
@JsonInclude(NON_NULL)
data class RiskManagementV2(
  val version: String?,
  val awaitingOtherInformation: String? = null,
  val emsInformation: String? = null,
  val emsInformationDetails: String? = null,
  val hasConsideredChecks: String? = null,
  val nonDisclosableInformation: String? = null,
  val nonDisclosableInformationDetails: String? = null,
  val proposedAddressSuitable: String? = null,
  val riskManagementDetails: String? = null,
  val unsuitableReason: String? = null,
) : RiskManagement

@JsonTypeName("3")
@JsonInclude(NON_NULL)
data class RiskManagementV3(
  val version: String?,
  val awaitingOtherInformation: String? = null,
  val emsInformation: String? = null,
  val emsInformationDetails: String? = null,
  val hasConsideredChecks: String? = null,
  val nonDisclosableInformation: String? = null,
  val nonDisclosableInformationDetails: String? = null,
  val proposedAddressSuitable: String? = null,
  val riskManagementDetails: String? = null,
  val unsuitableReason: String? = null,
  val manageInTheCommunity: String? = null,
  val manageInTheCommunityNotPossibleReason: String? = null,
  val pomConsultation: String? = null,
  val mentalHealthPlan: String? = null,
  val prisonHealthcareConsultation: String? = null,
) : RiskManagement

@JsonInclude(NON_NULL)
data class Victim(
  val victimLiaison: VictimLiaison?,
)

@JsonInclude(NON_NULL)
data class VictimLiaison(
  val decision: Decision?,
)

@JsonInclude(NON_NULL)
data class Approval(
  val release: Release,
  val consideration: DecisionMade? = null,
)

@JsonInclude(NON_NULL)
data class Release(
  val decision: Decision?,
  val decisionMaker: String?,
  val reasonForDecision: String?,
)

@JsonInclude(NON_NULL)
data class Document(
  val template: Template,
)

@JsonInclude(NON_NULL)
data class Template(
  val decision: String?,
  val offenceCommittedBeforeFeb2015: Decision?,
)

@JsonInclude(NON_NULL)
data class Reporting(
  val reportingInstructions: ReportingInstructions,
)

@JsonInclude(NON_NULL)
data class ReportingInstructions(
  val name: String?,
  val postcode: String?,
  val telephone: String?,
  val townOrCity: String?,
  val organisation: String?,
  val reportingDate: String?,
  val reportingTime: String?,
  val buildingAndStreet1: String?,
  val buildingAndStreet2: String?,
)

@JsonInclude(NON_NULL)
data class FinalChecks(
  val onRemand: DecisionMade? = null,
  val seriousOffence: DecisionMade? = null,
  val undulyLenientSentence: DecisionMade? = null,
  val confiscationOrder: ConfiscationOrder? = null,
  val segregation: DecisionMade? = null,
  val refund: Refusal? = null,
  val postpone: Postpone? = null,
)

@JsonInclude(NON_NULL)
data class Postpone(
  val version: String?,
  val decision: Decision?,
  val postponeReason: String?,
)

@JsonInclude(NON_NULL)
data class Refusal(
  val decision: Decision?,
  val outOfTimeReasons: String?,
  val reason: String?,
)

@JsonInclude(NON_NULL)
data class ConfiscationOrder(
  val comments: String?,
  val decision: Decision?,
  val confiscationUnitConsulted: Decision?,
)

@JsonInclude(NON_NULL)
data class BespokeCondition(
  val approved: String?,
  val text: String?,
)

@JsonInclude(NON_NULL)
data class LicenceConditions(
  val bespoke: List<BespokeCondition>?,
  val standard: Standard?,
  val additional: Map<String, Any>? = null,
  val conditionsSummary: ConditionsSummary?,
)

@JsonInclude(NON_NULL)
data class Standard(
  val additionalConditionsRequired: Decision?,
)

@JsonInclude(NON_NULL)
data class ConditionsSummary(
  val additionalConditionsJustification: String?,
)
