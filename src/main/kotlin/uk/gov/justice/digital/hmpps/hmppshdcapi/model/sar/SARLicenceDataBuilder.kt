package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AddressAndPhone
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Approval
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Cas2Offer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Curfew
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurrentCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FinalChecks
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Occupier
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.ProposedAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.RejectedCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Rejection
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Reporting
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Resident
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Vary
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.VaryApproval
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.LicenceConditionRenderer
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARCas2Offer
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getPolicyVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.policyVersions
import kotlin.String

/**
 * Extracts the surname from a full name in "firstname surname" format.
 * Returns the surname if the name contains at least one space, otherwise returns null.
 */
fun extractLastname(fullName: String?): String? {
  if (fullName.isNullOrBlank()) return fullName
  val trimmed = fullName.trim()
  val lastSpaceIndex = trimmed.lastIndexOf(' ')
  return if (lastSpaceIndex > 0) {
    trimmed.substring(lastSpaceIndex + 1)
  } else {
    null
  }
}

fun Licence.toSAR() = SARLicence(
  licenceId = this.id!!,
  stage = this.stage,
  version = this.version,
  transitionDate = this.transitionDate,
  varyVersion = this.varyVersion,
  additionalConditionsVersion = this.additionalConditionsVersion,
  standardConditionsVersion = this.standardConditionsVersion,
  deletedAt = this.deletedAt,
  licenceInCvl = this.licenceInCvl,
  licence = this.licence?.toSAR(additionalConditionsVersion),
)

fun LicenceVersion.toSAR() = SARLicenceVersion(
  licenceVersionId = this.id!!,
  timestamp = this.timestamp,
  version = this.version,
  template = this.template,
  varyVersion = this.varyVersion,
  deletedAt = this.deletedAt,
  licenceInCvl = this.licenceInCvl,
  licence = this.licence?.toSAR(),
)

fun LicenceData.toSAR(conditionVersion: Int? = null) = SARLicenceData(
  eligibility = this.eligibility,
  bassReferral = this.bassReferral.toSAR(),
  proposedAddress = this.proposedAddress.toSAR(),
  curfew = this.curfew.toSAR(),
  risk = this.risk,
  reporting = this.reporting.toSAR(),
  victim = this.victim,
  licenceConditions = this.licenceConditions?.let {
    SARConditions(
      bespokeConditions = it.bespoke,
      standard = it.standard,
      additionalConditions = it.additional?.entries?.map { (code, values) ->
        toAdditionalCondition(
          conditionVersion ?: attemptToGuessVersion(it.additional),
          code,
          values,
        )
      },
      additionalConditionsJustification = it.conditionsSummary?.additionalConditionsJustification,
    )
  },
  document = this.document,
  approval = this.approval.toSAR(),
  finalChecks = this.finalChecks.toSAR(),
  variedFromLicenceNotInSystem = this.variedFromLicenceNotInSystem,
  vary = this.vary.toSAR(),
  bassRejections = this.bassRejections?.map { it.toSAR() },
)

fun RejectedCas2Referral.toSAR() = SARRejectedCas2Referral(
  bassOffer = this.bassOffer?.toSAR(),
  bassRequest = this.bassRequest,
  approvedPremisesAddress = this.approvedPremisesAddress?.toAddress(),
  bassAreaCheck = this.bassAreaCheck,
  rejectionReason = this.rejectionReason,
  withdrawal = this.withdrawal,
)

fun FinalChecks?.toSAR() = if (this == null) {
  null
} else {
  SARFinalChecks(
    onRemand = this.onRemand,
    seriousOffence = this.seriousOffence,
    undulyLenientSentence = this.undulyLenientSentence,
    confiscationOrder = this.confiscationOrder,
    segregation = this.segregation,
    refusal = this.refusal?.let {
      SARRefusal(
        decision = it.decision,
        outOfTimeReasons = it.outOfTimeReasons?.items,
        reason = it.reason,
      )
    },
    postpone = this.postpone,
  )
}

fun Vary?.toSAR() = if (this == null) {
  null
} else {
  SARVary(
    approval = this.approval.toSAR(),
    evidence = this.evidence,
  )
}

fun VaryApproval?.toSAR() = if (this == null) {
  null
} else {
  SARVaryApproval(
    jobTitle = this.jobTitle,
    lastName = extractLastname(this.name),
  )
}

fun ProposedAddress?.toSAR() = if (this == null) {
  null
} else {
  SARProposedAddress(
    curfewAddress = curfewAddress?.toSAR(),
    addressProposed = addressProposed,
    optOut = optOut,
    rejections = rejections?.map { it.toSAR() },
  )
}

fun Rejection.toSAR() = SARRejection(
  address = address?.toSAR(),
  addressReview = addressReview,
  riskManagement = riskManagement,
  withdrawalReason = withdrawalReason,
)

fun CurfewAddress?.toSAR() = if (this == null) {
  null
} else {
  SARCurfewAddress(
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressTown = addressTown,
    postCode = postCode,
    occupier = occupier?.toSAR(),
    residents = residents?.map { it.toSAR() },
    additionalInformation = additionalInformation,
    residentOffenceDetails = residentOffenceDetails,
    cautionedAgainstResident = cautionedAgainstResident,
  )
}

fun Resident.toSAR() = SARResident(
  lastName = extractLastname(name),
  relationship = relationship,
  age = age,
)

fun Occupier?.toSAR() = if (this == null) {
  null
} else {
  SAROccupier(
    lastName = extractLastname(name),
    relationship = relationship,
    isOffender = isOffender,
  )
}

fun Curfew?.toSAR() = if (this == null) {
  null
} else {
  SARCurfew(
    addressWithdrawn = addressWithdrawn,
    consentWithdrawn = consentWithdrawn,
    firstNight = firstNight,
    curfewHours = curfewHours,
    approvedPremisesAddress = approvedPremisesAddress?.toAddress(),
    approvedPremises = approvedPremises,
    curfewAddressReview = curfewAddressReview,
  )
}

fun CurrentCas2Referral?.toSAR() = if (this == null) {
  null
} else {
  SARCurrentCas2Referral(
    bassOffer = bassOffer.toSAR(),
    bassRequest = bassRequest,
    approvedPremisesAddress = approvedPremisesAddress?.toAddress(),
    bassAreaCheck = bassAreaCheck,
    approvedPremises = approvedPremises,
    bassWithdrawn = bassWithdrawn,
  )
}

fun Cas2Offer?.toSAR() = if (this == null) {
  null
} else {
  SARCas2Offer(
    addressLine1 = this.addressLine1,
    addressLine2 = this.addressLine2,
    addressTown = this.addressTown,
    postCode = this.postCode,
    bassAccepted = this.bassAccepted,
    bassArea = this.bassArea,
    bassOfferDetails = this.bassOfferDetails,
  )
}

fun AddressAndPhone.toAddress() = AddressImpl(
  addressLine1 = this.addressLine1,
  addressLine2 = this.addressLine2,
  addressTown = this.addressTown,
  postCode = this.postCode,
)

fun Reporting?.toSAR() = if (this == null) {
  null
} else {
  SARReporting(
    reportingInstructions =
    with(this.reportingInstructions) {
      SARReportingInstructions(
        lastName = extractLastname(name),
        postcode = postcode,
        townOrCity = townOrCity,
        organisation = organisation,
        reportingDate = reportingDate,
        reportingTime = reportingTime,
        buildingAndStreet1 = buildingAndStreet1,
        buildingAndStreet2 = buildingAndStreet2,
      )
    },
  )
}

fun Approval?.toSAR(): SARApproval? = if (this == null) {
  null
} else {
  SARApproval(
    release = this.release?.let {
      SARRelease(
        decision = it.decision,
        decisionMakerLastName = extractLastname(it.decisionMaker),
        reasonForDecision = it.reasonForDecision,
        notedComments = it.notedComments,
        reason = it.reason?.items,
      )
    },
    consideration = this.consideration,
  )
}

fun attemptToGuessVersion(additional: Map<String, Map<String, Any>>?): Int? = policyVersions.getPolicyVersion(additional?.map { condition -> condition.key } ?: emptyList())

private fun toAdditionalCondition(
  conditionVersion: Int?,
  code: String,
  values: Map<String, Any>,
): SARAdditionalCondition {
  val conditionVersionData = LicenceConditionRenderer.getConditionTemplateVersion(conditionVersion)[code]!!
  val renderedTextData = LicenceConditionRenderer.renderCondition(conditionVersionData, values)

  return SARAdditionalCondition(
    renderedText = renderedTextData,
  )
}
