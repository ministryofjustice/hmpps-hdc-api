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

    val fieldMergeRules = mapOf(
      "appointmentDetails" to FieldMergeRule(
        fields = listOf("appointmentAddress", "appointmentDate", "appointmentTime"),
        delimiters = ON_AT,
      ),
      "appointmentDetailsInDrugsSection" to FieldMergeRule(
        fields = listOf(
          "appointmentAddressInDrugsSection",
          "appointmentDateInDrugsSection",
          "appointmentTimeInDrugsSection",
        ),
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
    val rule = fieldMergeRules[conditionMetaData.userInput] ?: return additionalFields
    val mutableFields = additionalFields.toMutableMap()

    val valuesInRuleOrder = rule.fields.mapIndexedNotNull { index, field ->
      mutableFields[field]?.toString()?.takeIf { it.isNotBlank() }?.let { index to it }
    }

    if (valuesInRuleOrder.isEmpty()) return additionalFields

    val mergedWithDelimiters = mergeFieldValues(valuesInRuleOrder, rule)

    // Remove merged fields
    rule.fields.forEach(mutableFields::remove)

    // Store merged value under the first rule field
    mutableFields[getFirstFieldOfRuleGroupKey(rule, conditionMetaData)] = mergedWithDelimiters

    return mutableFields
  }

  private fun getFirstFieldOfRuleGroupKey(
    rule: FieldMergeRule,
    conditionMetaData: ConditionMetadata,
  ): String = rule.fields.minBy { conditionMetaData.fieldPosition.getValue(it) }

  private fun mergeFieldValues(
    values: List<Pair<Int, String>>,
    rule: FieldMergeRule,
  ): String {
    val builder = StringBuilder()
    var first = true

    values.forEach { (ruleIndex, value) ->
      if (first) {
        builder.append(value)
        first = false
      } else {
        // Pick delimiter based on the previous ruleIndex
        val delimiter = rule.delimiters.getOrElse(ruleIndex - 1) { rule.delimiters.last() }
        builder.append(delimiter).append(value)
      }
    }

    return builder.toString()
  }
}
