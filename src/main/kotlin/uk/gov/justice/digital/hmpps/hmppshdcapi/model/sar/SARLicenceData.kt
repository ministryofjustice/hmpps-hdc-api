package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Approval
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.BespokeCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Curfew
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurrentCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Decision
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.DecisionMade
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Document
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Eligibility
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FinalChecks
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStage
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.ProposedAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.RejectedCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Reporting
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Risk
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Standard
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Vary
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Victim
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.LicenceConditionRenderer
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getConditionText
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getConditionValues
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getPolicyVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.policyVersions
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.StringListHolder
import java.time.LocalDateTime
import kotlin.Boolean

@JsonInclude(NON_NULL)
data class SARLicence(
  var licenceId: Long,
  var prisonNumber: String,
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
  var prisonNumber: String?,
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
  val bassReferral: CurrentCas2Referral?,
  val proposedAddress: ProposedAddress?,
  val curfew: Curfew?,
  val risk: Risk?,
  val reporting: SARReporting?,
  val victim: Victim?,
  val licenceConditions: SARConditions?,
  val document: Document?,
  val approval: SARApproval?,
  val finalChecks: FinalChecks?,
  val variedFromLicenceNotInSystem: Boolean? = null,
  val vary: Vary? = null,
  val bassRejections: List<RejectedCas2Referral>? = null,
)

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
  // Can be a single value or an array
  val reason: StringListHolder? = null,
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
  val text: String? = null,
  val fields: Map<String, Any>? = null,
  val renderedText: String? = null,
)

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
  prisonNumber = this.prisonNumber,
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
  prisonNumber = this.prisonNumber,
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
  bassReferral = this.bassReferral,
  proposedAddress = this.proposedAddress,
  curfew = this.curfew,
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
  finalChecks = this.finalChecks,
  variedFromLicenceNotInSystem = this.variedFromLicenceNotInSystem,
  vary = this.vary,
  bassRejections = this.bassRejections,
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
        reason = it.reason,
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
    text = policyVersions.getConditionText(conditionVersion, code),
    fields = policyVersions.getConditionValues(values),
    renderedText = renderedTextData,
  )
}
