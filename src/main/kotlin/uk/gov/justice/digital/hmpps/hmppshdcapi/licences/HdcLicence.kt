package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.DayOfWeek
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
  val firstNight: FirstNight?,
  val curfewHours: CurfewHours?,
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
data class CurfewTimes(
  val fromDay: DayOfWeek,
  @JsonFormat(pattern = "HH:mm")
  val fromTime: LocalTime,
  val untilDay: DayOfWeek,
  @JsonFormat(pattern = "HH:mm")
  val untilTime: LocalTime,
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
data class LicenceConditions(
  val bespoke: List<String>?,
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
  @JsonProperty("ALCOHOL_MONITORING")
  val alcoholMonitoring: Any?,

  @JsonProperty("ALLOW_POLICE_SEARCH")
  val allowPoliceSearch: Any?,

  @JsonAlias("ATTEND_ALL", "ATTENDALL")
  val attendAll: Any?,

  @JsonProperty("ATTENDDEPENDENCY")
  val attendingDependency: Any?,

  @JsonAlias("ATTEND_DEPENDENCY_IN_DRUGS_SECTION", "ATTENDDEPENDENCYINDRUGSSECTION")
  val attendDependencyInDrugsSection: Any?,

  @JsonProperty("ATTEND_SAMPLE")
  val attendSample: Any?,

  @JsonProperty("CAMERA_APPROVAL")
  val cameraApproval: Any?,

  @JsonAlias("COMPLY_REQUIREMENTS", "COMPLYREQUIREMENTS")
  val complyRequirements: Any?,

  @JsonAlias("CONFINE_ADDRESS", "CONFINEADDRESS")
  val confineAddress: Any?,

  @JsonProperty("CURFEW_UNTIL_INSTALLATION")
  val curfewUntilInstallation: Any?,

  @JsonProperty("DONT_HAMPER_DRUG_TESTING")
  val dontHamperDrugTesting: Any?,

  @JsonProperty("DRUG_TESTING")
  val drugTesting: Any?,

  @JsonProperty("ELECTRONIC_MONITORING_INSTALLATION")
  val electronMonitoringInstallation: Any?,

  @JsonProperty("ELECTRONIC_MONITORING_TRAIL")
  val electronMonitoringTrail: Any?,

  @JsonAlias("EXCLUSION_ADDRESS", "EXCLUSIONADDRESS")
  val exclusionAddress: Any?,

  @JsonAlias("EXCLUSION_AREA", "EXCLUSIONAREA")
  val exclusionArea: Any?,

  @JsonAlias("HOME_VISITS", "HOMEVISITS")
  val homeVisits: Any?,

  @JsonAlias("INTIMATE_RELATIONSHIP", "INTIMATERELATIONSHIP")
  val intimateRelationship: Any?,

  @JsonProperty("NO_CAMERA")
  val noCamera: Any?,

  @JsonAlias("NO_CAMERA_PHONE", "NOCAMERAPHONE")
  val noCameraPhone: Any?,

  @JsonAlias("NO_CHILDRENS_AREA", "NOCHILDRENSAREA")
  val noChildrenArea: Any?,

  @JsonAlias("NO_COMMUNICATE_VICTIM", "NOCOMMUNICATEVICTIM")
  val noCommunicateVictim: Any?,

  @JsonProperty("NO_CONTACT_ASSOCIATE")
  val noContactAssociate: Any?,

  @JsonAlias("NO_CONTACT_NAMED", "NOCONTACTNAMED")
  val noContactNamed: Any?,

  @JsonAlias("NO_CONTACT_PRISONER", "NOCONTACTPRISONER")
  val noContactPrisoner: Any?,

  @JsonProperty("NO_CONTACT_SEX_OFFENDER")
  val noContactSexOffender: Any?,

  @JsonAlias("NO_INTERNET", "NOINTERNET")
  val noInternet: Any?,

  @JsonAlias("NO_RESIDE", "NORESIDE")
  val noReside: Any?,

  @JsonAlias("NOTIFY_PASSPORT", "NOTIFYPASSPORT")
  val notifyPassport: Any?,

  @JsonAlias("NOTIFY_RELATIONSHIP", "NOTIFYRELATIONSHIP")
  val notifyRelationship: Any?,

  @JsonAlias("NO_UNSUPERVISED_CONTACT", "NOUNSUPERVISEDCONTACT")
  val noUnsupervisedContact: Any?,

  @JsonAlias("NO_WORK_WITH_AGE", "NOWORKWITHAGE")
  val noWorkWithAge: Any?,

  @JsonAlias("ONE_PHONE", "ONEPHONE")
  val onePhone: Any?,

  @JsonProperty("POLICE_ESCORT")
  val policeEscort: Any?,

  @JsonProperty("POLYGRAPH")
  val polygraph: Any?,

  @JsonAlias("REMAIN_ADDRESS", "REMAINADDRESS")
  val remainAddress: Any?,

  @JsonAlias("REPORT_TO", "REPORTTO")
  val reportTo: Any?,

  @JsonProperty("RESIDE_AT_SPECIFIC_PLACE")
  val resideAtSpecificPlace: Any?,

  @JsonProperty("RETURN_TO_UK")
  val returnToUk: Any?,

  @JsonProperty("SPECIFIC_ITEM")
  val specificItem: Any?,

  @JsonAlias("SURRENDER_PASSPORT", "SURRENDERPASSPORT")
  val surrenderPassport: Any?,

  @JsonAlias("USAGE_HISTORY", "USAGEHISTORY")
  val usageHistory: Any?,

  @JsonAlias("VEHICLE_DETAILS", "VEHICLEDETAILS")
  val vehicleDetails: Any?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionsSummary(
  val additionalConditionsJustification: String?,
)
