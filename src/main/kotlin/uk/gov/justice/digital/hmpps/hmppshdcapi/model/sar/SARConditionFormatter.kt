package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.ConditionMetadata
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.V1_CONDITIONS
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.V2_CONDITIONS

typealias ConditionPolicies = Map<Int, List<ConditionMetadata>>

object SARConditionFormatter {

  val policyVersions = mapOf(1 to V1_CONDITIONS, 2 to V2_CONDITIONS)

  // Map of condition to version (but only include conditions that only appear in a single policy version)
  private val conditionToPolicyVersion =
    policyVersions.entries
      .flatMap { it.value }
      .groupingBy { it.id }
      .eachCount()
      .filter { it.value == 1 }
      .map { it.key to policyVersions.findVersion(it.key) }.toMap()

  fun ConditionPolicies.getConditionText(conditionVersion: Int?, id: String): String = policyVersions[conditionVersion]?.find { it.id == id }?.text ?: id

  fun ConditionPolicies.getConditionValues(values: Map<String, Any>): Map<String, Any>? = values.takeIf { it.isNotEmpty() }?.mapKeys { it.key.camelToSentenceCase() }

  fun ConditionPolicies.getPolicyVersion(conditionIds: Collection<String>) = conditionIds.mapNotNull { conditionToPolicyVersion[it] }.distinct().singleOrNull()

  private fun ConditionPolicies.findVersion(id: String) = entries.find { (_, conditions) -> conditions.any { it.id == id } }?.key

  fun String.camelToSentenceCase(): String {
    return this
      .replace("_", " ") // Handle snake_case
      .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ") // camelCase → space before capital
      .replace(Regex("(?<=[A-Z])(?=[A-Z][a-z])"), " ") // handle acronyms like JSONData
      .replace(Regex("(?<=[a-zA-Z])(?=[0-9])"), " ") // space before numbers
      .lowercase() // make all lowercase
      .replaceFirstChar { it.titlecase() } // capitalize only the first word
  }
}
