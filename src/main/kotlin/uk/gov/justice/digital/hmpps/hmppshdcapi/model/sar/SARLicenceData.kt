package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Address
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AddressReview
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AddressReviewWrapper
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.ApprovedPremises
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.BespokeCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Cas2AreaCheck
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Cas2Request
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.ConfiscationOrder
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Decision
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.DecisionMade
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Document
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Eligibility
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.EnterNewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Evidence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStage
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.OfferAccepted
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Postpone
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.RejectedCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.RejectedRiskManagement
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Risk
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Standard
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Victim
import java.time.LocalDateTime

@JsonInclude(NON_NULL)
data class SARLicence(
  var licenceId: Long,
  val stage: HdcStage,
  val version: Int,
  val transitionDate: LocalDateTime?,
  val varyVersion: Int,
  val additionalConditionsVersion: Int?,
  val standardConditionsVersion: Int?,
  var deletedAt: LocalDateTime?,
  var licenceInCvl: Boolean,
  val licence: SARLicenceData?,
)

@JsonInclude(NON_NULL)
class SARLicenceVersion(
  var licenceVersionId: Long,
  val timestamp: LocalDateTime,
  val version: Int,
  val template: String,
  val varyVersion: Int,
  var deletedAt: LocalDateTime?,
  var licenceInCvl: Boolean,
  val licence: SARLicenceData?,
)

@JsonInclude(NON_NULL)
data class SARLicenceData(
  val eligibility: Eligibility?,
  val bassReferral: SARCurrentCas2Referral?,
  val proposedAddress: SARProposedAddress?,
  val curfew: SARCurfew?,
  val risk: Risk?,
  val reporting: SARReporting?,
  val victim: Victim?,
  val licenceConditions: SARConditions?,
  val document: Document?,
  val approval: SARApproval?,
  val finalChecks: SARFinalChecks?,
  val variedFromLicenceNotInSystem: Boolean? = null,
  val vary: SARVary? = null,
  val bassRejections: List<RejectedCas2Referral>? = null,
)

@JsonInclude(NON_NULL)
data class SARFinalChecks(
  val onRemand: DecisionMade? = null,
  val seriousOffence: DecisionMade? = null,
  val undulyLenientSentence: DecisionMade? = null,
  val confiscationOrder: ConfiscationOrder? = null,
  val segregation: DecisionMade? = null,
  val refusal: SARRefusal? = null,
  val postpone: Postpone? = null,
)

@JsonInclude(NON_NULL)
data class SARRefusal(
  val decision: Decision?,
  val outOfTimeReasons: List<String>? = null,
  val reason: String?,
)

@JsonInclude(NON_NULL)
data class SARProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: SARCurfewAddress? = null,
  val addressProposed: DecisionMade? = null,
  val optOut: DecisionMade? = null,
  val rejections: List<SARRejection>? = null,
)

@JsonInclude(NON_NULL)
data class SARRejection(
  val address: SARCurfewAddress?,
  val addressReview: AddressReviewWrapper? = null,
  val riskManagement: RejectedRiskManagement? = null,
  val withdrawalReason: String? = null,
)

@JsonInclude(NON_NULL)
data class SARCurfewAddress(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
  val occupier: SAROccupier? = null,
  val residents: List<SARResident>? = null,
  val additionalInformation: String? = null,
  val residentOffenceDetails: String? = null,
  val cautionedAgainstResident: Decision? = null,
) : Address

@JsonInclude(NON_NULL)
data class SAROccupier(
  val lastName: String? = null,
  val relationship: String? = null,
  val isOffender: String? = null,
)

@JsonInclude(NON_NULL)
data class SARResident(
  val age: String? = null,
  val lastName: String? = null,
  val relationship: String? = null,
)

@JsonInclude(NON_NULL)
data class SARCurrentCas2Referral(
  // bassOffer only nullable as address will be either this if Cas2Referral or curfewAddress if proposed address
  val bassOffer: SARCas2Offer? = null,
  val bassRequest: Cas2Request? = null,
  val approvedPremisesAddress: Address? = null,
  val bassAreaCheck: Cas2AreaCheck? = null,
  val approvedPremises: ApprovedPremises? = null,
  val bassWithdrawn: DecisionMade? = null,
)

@JsonInclude(NON_NULL)
data class SARCas2Offer(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
  val bassAccepted: OfferAccepted?,
  val bassArea: String? = null,
  val bassOfferDetails: String? = null,
) : Address

@JsonInclude(NON_NULL)
data class SARApproval(
  val release: SARRelease? = null,
  val consideration: DecisionMade? = null,
)

@JsonInclude(NON_NULL)
data class SARRelease(
  val decision: Decision?,
  val decisionMakerLastName: String?,
  val reasonForDecision: String?,
  val notedComments: String? = null,
  val reason: List<String>? = null,
)

@JsonInclude(NON_NULL)
data class SARReporting(
  val reportingInstructions: SARReportingInstructions,
)

@JsonInclude(NON_NULL)
data class SARReportingInstructions(
  val lastName: String?,
  val postcode: String?,
  val townOrCity: String?,
  val organisation: String?,
  val reportingDate: String?,
  val reportingTime: String?,
  val buildingAndStreet1: String?,
  val buildingAndStreet2: String?,
)

@JsonInclude(NON_NULL)
data class SARConditions(
  val bespokeConditions: List<BespokeCondition>? = null,
  val additionalConditions: List<SARAdditionalCondition>? = null,
  val standard: Standard? = null,
  val additionalConditionsJustification: String? = null,
)

@JsonInclude(NON_NULL)
data class SARAdditionalCondition(
  val renderedText: String? = null,
)

@JsonInclude(NON_NULL)
data class SARVary(
  val approval: SARVaryApproval? = null,
  val evidence: Evidence? = null,
)

@JsonInclude(NON_NULL)
data class SARVaryApproval(
  val jobTitle: String? = null,
  val lastName: String? = null,
)

@JsonInclude(NON_NULL)
data class SARCurfew(
  val addressWithdrawn: EnterNewAddress? = null,
  val consentWithdrawn: EnterNewAddress? = null,
  val firstNight: FirstNight?,
  val curfewHours: CurfewHours?,
  val approvedPremisesAddress: Address? = null,
  val approvedPremises: ApprovedPremises? = null,
  val curfewAddressReview: AddressReview? = null,
)

@JsonInclude(NON_NULL)
data class AddressImpl(
  override val addressLine1: String? = null,
  override val addressLine2: String? = null,
  override val addressTown: String? = null,
  override val postCode: String? = null,
) : Address
