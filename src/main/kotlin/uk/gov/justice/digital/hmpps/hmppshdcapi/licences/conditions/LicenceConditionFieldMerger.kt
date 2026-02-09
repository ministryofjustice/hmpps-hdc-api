package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

class LicenceConditionFieldMerger {

  data class FieldMergeRule(
    val fields: List<String>,
    val delimiters: List<String>,
  )

  companion object {

    private val ON_AT = listOf(" on ", " at ")
    private val COMMA = listOf(", ")
    private val ENDING_ON = listOf(" ending on ")

    val fieldMergeRules: Map<String, FieldMergeRule> = mapOf(
      "appointmentDetails" to FieldMergeRule(
        fields = listOf("appointmentAddress", "appointmentDate", "appointmentTime"),
        delimiters = ON_AT,
      ),
      "appointmentDetailsInDrugsSection" to FieldMergeRule(
        fields = listOf("appointmentAddressInDrugsSection", "appointmentDateInDrugsSection", "appointmentTimeInDrugsSection"),
        delimiters = ON_AT,
      ),
      "attendSampleDetails" to FieldMergeRule(
        fields = listOf("attendSampleDetailsName", "attendSampleDetailsAddress"),
        delimiters = COMMA,
      ),
      "drug_testing" to FieldMergeRule(
        fields = listOf("drug_testing_name", "drug_testing_address"),
        delimiters = COMMA,
      ),
      "alcoholMonitoring" to FieldMergeRule(
        fields = listOf("timeframe", "endDate"),
        delimiters = ENDING_ON,
      ),
    )
  }

  fun mergeIfRequired(
    conditionMetaData: ConditionMetadata,
    additionalFields: Map<String, Any>,
  ): Map<String, Any> {
    val inProcessAdditionalFields = additionalFields.toMutableMap()
    val rule = fieldMergeRules[conditionMetaData.userInput] ?: return additionalFields

    val values = rule.fields.mapNotNull { field ->
      inProcessAdditionalFields[field]?.toString()?.takeIf { it.isNotBlank() }
    }

    if (values.isNotEmpty()) {
      val merged = mergeValues(rule, inProcessAdditionalFields)
      rule.fields.forEach { inProcessAdditionalFields.remove(it) }
      inProcessAdditionalFields[getLowestRuleFieldKey(conditionMetaData, rule)] = merged
    }

    return inProcessAdditionalFields
  }

  fun getLowestRuleFieldKey(
    condition: ConditionMetadata,
    rule: FieldMergeRule,
  ): String = rule.fields.minBy { condition.fieldPosition.getValue(it) }

  private fun mergeValues(
    rule: FieldMergeRule,
    additionalFields: Map<String, Any>,
  ): String {
    val result = StringBuilder()

    rule.fields.forEachIndexed { index, field ->
      val value = additionalFields[field]?.toString()?.takeIf { it.isNotBlank() } ?: return@forEachIndexed

      if (result.isNotEmpty()) {
        val delimiter = rule.delimiters.getOrElse(index - 1) { rule.delimiters.last() }
        result.append(delimiter)
      }

      result.append(value)
    }

    return result.toString()
  }
}
