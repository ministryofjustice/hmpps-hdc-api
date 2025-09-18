package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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
  val document: Document?,
  val approval: Approval?,
  val finalChecks: FinalChecks?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Eligibility(
  val crdTime: DecisionMade?,
  val excluded: DecisionMade?,
  val suitability: DecisionMade?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DecisionMade(
  val decision: Decision?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Referral(
  // bassOffer only nullable as address will be either this if Cas2Referral or curfewAddress if proposed address
  val bassOffer: Cas2Offer? = null,
  val bassRequest: Cas2Request? = null,
  val approvedPremisesAddress: CurfewAddress? = null,
  val bassAreaCheck: Cas2AreaCheck? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: CurfewAddress? = null,
  val addressProposed: AddressProposed? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurfewAddress(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
) : Address

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddressProposed(
  val decision: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Offer(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
  val bassAccepted: OfferAccepted?,
) : Address

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Request(
  val bassRequested: Decision?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Curfew(
  val firstNight: FirstNight?,
  val curfewHours: CurfewHours?,
  val approvedPremisesAddress: CurfewAddress? = null,
  val approvedPremises: ApprovedPremises? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FirstNight(
  val firstNightFrom: LocalTime,
  val firstNightUntil: LocalTime,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurfewHours(
  val mondayFrom: LocalTime?,
  val mondayUntil: LocalTime?,
  val tuesdayFrom: LocalTime?,
  val tuesdayUntil: LocalTime?,
  val wednesdayFrom: LocalTime?,
  val wednesdayUntil: LocalTime?,
  val thursdayFrom: LocalTime?,
  val thursdayUntil: LocalTime?,
  val fridayFrom: LocalTime?,
  val fridayUntil: LocalTime?,
  val saturdayFrom: LocalTime?,
  val saturdayUntil: LocalTime?,
  val sundayFrom: LocalTime?,
  val sundayUntil: LocalTime?,
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
data class Risk(
  val riskManagement: RiskManagement,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RiskManagement(
  val version: String,
  val emsInformation: Decision?,
  val pomConsultation: Decision?,
  val mentalHealthPlan: Decision?,
  val manageInTheCommunity: Decision?,
  val unsuitableReason: String?,
  val hasConsideredChecks: Decision?,
  val emsInformationDetails: String?,
  val riskManagementDetails: String?,
  val proposedAddressSuitable: Decision?,
  val awaitingOtherInformation: Decision?,
  val nonDisclosableInformation: Decision?,
  val nonDisclosableInformationDetails: String?,
  val manageInTheCommunityNotPossibleReason: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Victim(
  val victimLiaison: VictimLiaison?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VictimLiaison(
  val decision: Decision?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Approval(
  val release: Release,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Release(
  val decision: Decision?,
  val decisionMaker: String?,
  val reasonForDecision: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document(
  val template: Template,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Template(
  val decision: String?,
  val offenceCommittedBeforeFeb2015: Decision?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Reporting(
  val reportingInstructions: ReportingInstructions,
)

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class FinalChecks(
  val onRemand: OnRemand?,
  val seriousOffence: SeriousOffence?,
  val confiscationOrder: ConfiscationOrder?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnRemand(
  val decision: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeriousOffence(
  val decision: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfiscationOrder(
  val decision: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BespokeCondition(
  val approved: String?,
  val text: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LicenceConditions(
  val bespoke: List<BespokeCondition>?,
  val standard: Standard?,
  val additional: Additional?,
  val conditionsSummary: ConditionsSummary?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Standard(
  val additionalConditionsRequired: Decision?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Additional(
  @param:JsonProperty("ALCOHOL_MONITORING")
  val alcoholMonitoring: Any?,

  @param:JsonProperty("ALLOW_POLICE_SEARCH")
  val allowPoliceSearch: Any?,

  @param:JsonAlias("ATTEND_ALL", "ATTENDALL")
  val attendAll: Any?,

  @param:JsonProperty("ATTENDDEPENDENCY")
  val attendingDependency: Any?,

  @param:JsonAlias("ATTEND_DEPENDENCY_IN_DRUGS_SECTION", "ATTENDDEPENDENCYINDRUGSSECTION")
  val attendDependencyInDrugsSection: Any?,

  @param:JsonProperty("ATTEND_SAMPLE")
  val attendSample: Any?,

  @param:JsonProperty("CAMERA_APPROVAL")
  val cameraApproval: Any?,

  @param:JsonAlias("COMPLY_REQUIREMENTS", "COMPLYREQUIREMENTS")
  val complyRequirements: Any?,

  @param:JsonAlias("CONFINE_ADDRESS", "CONFINEADDRESS")
  val confineAddress: Any?,

  @param:JsonProperty("CURFEW_UNTIL_INSTALLATION")
  val curfewUntilInstallation: Any?,

  @param:JsonProperty("DONT_HAMPER_DRUG_TESTING")
  val dontHamperDrugTesting: Any?,

  @param:JsonProperty("DRUG_TESTING")
  val drugTesting: Any?,

  @param:JsonProperty("ELECTRONIC_MONITORING_INSTALLATION")
  val electronMonitoringInstallation: Any?,

  @param:JsonProperty("ELECTRONIC_MONITORING_TRAIL")
  val electronMonitoringTrail: Any?,

  @param:JsonAlias("EXCLUSION_ADDRESS", "EXCLUSIONADDRESS")
  val exclusionAddress: Any?,

  @param:JsonAlias("EXCLUSION_AREA", "EXCLUSIONAREA")
  val exclusionArea: Any?,

  @param:JsonAlias("HOME_VISITS", "HOMEVISITS")
  val homeVisits: Any?,

  @param:JsonAlias("INTIMATE_RELATIONSHIP", "INTIMATERELATIONSHIP")
  val intimateRelationship: Any?,

  @param:JsonProperty("NO_CAMERA")
  val noCamera: Any?,

  @param:JsonAlias("NO_CAMERA_PHONE", "NOCAMERAPHONE")
  val noCameraPhone: Any?,

  @param:JsonAlias("NO_CHILDRENS_AREA", "NOCHILDRENSAREA")
  val noChildrenArea: Any?,

  @param:JsonAlias("NO_COMMUNICATE_VICTIM", "NOCOMMUNICATEVICTIM")
  val noCommunicateVictim: Any?,

  @param:JsonProperty("NO_CONTACT_ASSOCIATE")
  val noContactAssociate: Any?,

  @param:JsonAlias("NO_CONTACT_NAMED", "NOCONTACTNAMED")
  val noContactNamed: Any?,

  @param:JsonAlias("NO_CONTACT_PRISONER", "NOCONTACTPRISONER")
  val noContactPrisoner: Any?,

  @param:JsonProperty("NO_CONTACT_SEX_OFFENDER")
  val noContactSexOffender: Any?,

  @param:JsonAlias("NO_INTERNET", "NOINTERNET")
  val noInternet: Any?,

  @param:JsonAlias("NO_RESIDE", "NORESIDE")
  val noReside: Any?,

  @param:JsonAlias("NOTIFY_PASSPORT", "NOTIFYPASSPORT")
  val notifyPassport: Any?,

  @param:JsonAlias("NOTIFY_RELATIONSHIP", "NOTIFYRELATIONSHIP")
  val notifyRelationship: Any?,

  @param:JsonAlias("NO_UNSUPERVISED_CONTACT", "NOUNSUPERVISEDCONTACT")
  val noUnsupervisedContact: Any?,

  @param:JsonAlias("NO_WORK_WITH_AGE", "NOWORKWITHAGE")
  val noWorkWithAge: Any?,

  @param:JsonAlias("ONE_PHONE", "ONEPHONE")
  val onePhone: Any?,

  @param:JsonProperty("POLICE_ESCORT")
  val policeEscort: Any?,

  @param:JsonProperty("POLYGRAPH")
  val polygraph: Any?,

  @param:JsonAlias("REMAIN_ADDRESS", "REMAINADDRESS")
  val remainAddress: Any?,

  @param:JsonAlias("REPORT_TO", "REPORTTO")
  val reportTo: Any?,

  @param:JsonProperty("RESIDE_AT_SPECIFIC_PLACE")
  val resideAtSpecificPlace: Any?,

  @param:JsonProperty("RETURN_TO_UK")
  val returnToUk: Any?,

  @param:JsonProperty("SPECIFIC_ITEM")
  val specificItem: Any?,

  @param:JsonAlias("SURRENDER_PASSPORT", "SURRENDERPASSPORT")
  val surrenderPassport: Any?,

  @param:JsonAlias("USAGE_HISTORY", "USAGEHISTORY")
  val usageHistory: Any?,

  @param:JsonAlias("VEHICLE_DETAILS", "VEHICLEDETAILS")
  val vehicleDetails: Any?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionsSummary(
  val additionalConditionsJustification: String?,
)
