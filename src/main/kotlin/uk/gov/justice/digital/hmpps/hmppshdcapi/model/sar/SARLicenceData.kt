package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Approval
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.BespokeCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Curfew
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurrentCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Document
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Eligibility
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FinalChecks
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.ProposedAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.RejectedCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Reporting
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Risk
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Vary
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Victim
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getConditionText
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getConditionValues
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getPolicyVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.policyVersions
import java.time.LocalDateTime
import kotlin.Boolean

@JsonInclude(NON_NULL)
data class SARLicence(
  var prisonNumber: String,
  val stage: String,
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
  val reporting: Reporting?,
  val victim: Victim?,
  val licenceConditions: SARConditions?,
  val document: Document?,
  val approval: Approval?,
  val finalChecks: FinalChecks?,
  val variedFromLicenceNotInSystem: Boolean? = null,
  val vary: Vary? = null,
  val bassRejections: List<RejectedCas2Referral>? = null,
)

@JsonInclude(NON_NULL)
data class SARConditions(
  val bespokeConditions: List<BespokeCondition>? = null,
  val additionalConditions: List<SARAdditionalCondition>? = null,
  val additionalConditionsJustification: String? = null,
)

@JsonInclude(NON_NULL)
data class SARAdditionalCondition(
  val text: String? = null,
  val fields: Map<String, Any>? = null,
)

fun Licence.toSAR() = SARLicence(
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
  reporting = this.reporting,
  victim = this.victim,
  licenceConditions = this.licenceConditions?.let {
    SARConditions(
      bespokeConditions = it.bespoke,
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
  approval = this.approval,
  finalChecks = this.finalChecks,
  variedFromLicenceNotInSystem = this.variedFromLicenceNotInSystem,
  vary = this.vary,
  bassRejections = this.bassRejections,
)

private fun attemptToGuessVersion(additional: Map<String, Map<String, Any>>?): Int? = policyVersions.getPolicyVersion(additional?.map { condition -> condition.key } ?: emptyList())

private fun LicenceData.toAdditionalCondition(conditionVersion: Int?, code: String, values: Map<String, Any>) = SARAdditionalCondition(
  text = policyVersions.getConditionText(conditionVersion, code),
  fields = policyVersions.getConditionValues(values),
)
