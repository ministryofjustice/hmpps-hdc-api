package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy

import com.fasterxml.jackson.annotation.JsonIgnore
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy.ConditionChangeType.DELETED
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy.ConditionChangeType.REMOVED_NO_REPLACEMENTS
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy.ConditionChangeType.REPLACED

enum class ConditionChangeType {
  /**
   * Deleted conditions are those whose replacements contain one or more existing conditions,
   * in other words, the replacements are suggestions of existing conditions that may be appropriate, or may not.
   */
  DELETED,

  /**
   * Replaced conditions are those whose replacements are all new conditions,
   * in other words, the replacements have been added as explicit replacements for the deleted condition.
   */
  REPLACED,

  REMOVED_NO_REPLACEMENTS,
  NEW_OPTIONS,
  TEXT_CHANGE,
}

data class SuggestedCondition(
  val code: String,
  val currentText: String,
)

data class LicenceConditionChanges(
  val changeType: ConditionChangeType,
  val code: String,
  val sequence: Int?,
  val previousText: String,
  val currentText: String?,
  @JsonIgnore var addedInputs: List<Any>,
  @JsonIgnore var removedInputs: List<Any>,
  val suggestions: List<SuggestedCondition> = emptyList(),
)

data class Replacements(
  val code: String,
  val changeType: ConditionChangeType,
  val alternatives: List<ILicenceCondition>,
)

fun getSuggestedReplacements(previous: LicencePolicy?, current: LicencePolicy): List<Replacements> {
  val previousConditions = previous?.allAdditionalConditions()?.associateBy { it.code } ?: emptyMap()
  val currentConditions = current.allAdditionalConditions().associateBy { it.code }

  return when (previous) {
    null -> emptyList()
    else ->
      previous.allAdditionalConditions()
        .filterNot { currentConditions.containsKey(it.code) }
        .map { condition ->

          val replacements = current.changeHints.find { it.previousCode == condition.code }?.replacements ?: emptyList()

          val type = when {
            replacements.isEmpty() -> REMOVED_NO_REPLACEMENTS
            replacements.any { previousConditions.containsKey(it) } -> DELETED
            else -> REPLACED
          }

          val replacementConditions = replacements.mapNotNull { currentConditions[it] ?: previousConditions[it] }

          Replacements(condition.code, type, replacementConditions)
        }
  }
}
