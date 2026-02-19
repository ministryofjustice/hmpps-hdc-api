package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedBespokeCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.attemptToGuessVersion
import kotlin.collections.associateBy

private const val ARRAY_SEPARATOR = ", "

object LicenceConditionRenderer {

  private val conditionFieldMerger = LicenceConditionFieldMerger()

  fun renderConditions(licenceData: LicenceData?, conditionVersion: Int? = null): List<ConvertedBespokeCondition> {
    val licenceConditions = licenceData?.licenceConditions ?: return emptyList()
    val additionalData = licenceConditions.additional ?: return emptyList()

    val conditionVersion = conditionVersion ?: attemptToGuessVersion(additionalData)
    val conditionTemplate = getConditionTemplateVersion(conditionVersion)

    return additionalData.mapNotNull { (conditionId, additionalFields) ->
      val conditionMeta = conditionTemplate[conditionId] ?: return@mapNotNull null
      conditionId to renderCondition(conditionMeta, additionalFields)

      ConvertedBespokeCondition(conditionId, renderCondition(conditionMeta, additionalFields))
    }
  }

  fun renderConditions(licence: Licence, conditionVersion: Int? = null): List<ConvertedBespokeCondition> = renderConditions(licence.licence, conditionVersion)

  fun renderCondition(
    conditionMeta: ConditionMetadata,
    additionalFields: Map<String, Any>,
  ): String {
    val condensedFields = conditionFieldMerger.mergeIfRequired(conditionMeta, additionalFields)
    val cleanedConditionMetaData = removeUnwantedFieldsFromMetaDataWhenRequired(conditionMeta, condensedFields)
    val orderedFieldsData = getFieldsDataInCorrectPositions(cleanedConditionMetaData, condensedFields)
    return renderDataToText(conditionMeta.text, orderedFieldsData)
  }

  fun renderDataToText(
    textTemplate: String,
    fieldsData: List<Any>,
  ): String {
    var rendered = textTemplate
    fieldsData.forEachIndexed { index, value ->
      val regex = Regex("\\[.*?]")
      rendered = rendered.replaceFirst(regex, convertToString(value))
    }
    // This is a one off fix as sometimes problem occours with REPORT_TO
    return rendered.replace(" ,", ",")
      .replace(Regex("\\s+\\.$"), ".")
      .replace(Regex("\\.{2,}\\s*$"), ".")
  }

  private fun removeUnwantedFieldsFromMetaDataWhenRequired(
    conditionMetaData: ConditionMetadata,
    additionalFields: Map<String, Any>,
  ): ConditionMetadata {
    var inProcessConditionMetaData = conditionMetaData

    // This is a bit of a hack between version 1 and 2 of the condition meta data (see version info)
    // This will have to change if we have a different version in the future
    when (inProcessConditionMetaData.id) {
      "REPORTTO" -> {
        val fieldPosition = inProcessConditionMetaData.fieldPosition.toMutableMap()
        fieldPosition["reportingFrequency"] = 2
        if (!additionalFields.containsKey("reportingTime")) {
          fieldPosition["reportingDaily"] = 1
          fieldPosition["reportingTime"] = 5
        } else {
          fieldPosition["reportingDaily"] = 5
        }

        inProcessConditionMetaData = inProcessConditionMetaData.copy(fieldPosition = fieldPosition)
      }
    }

    return inProcessConditionMetaData
  }

  private fun getFieldsDataInCorrectPositions(
    conditionMeta: ConditionMetadata,
    additionalFields: Map<String, Any>,
  ): List<Any> = conditionMeta.fieldPosition.entries
    .sortedBy { it.value }
    .map {
      additionalFields[it.key] ?: ""
    }

  fun getConditionTemplateVersion(conditionVersion: Int?): Map<String, ConditionMetadata> = (if (conditionVersion == 1) V1_CONDITIONS else V2_CONDITIONS).associateBy { it.id }

  private fun convertToString(value: Any): String = when (value) {
    is Array<*> -> value.joinToString(ARRAY_SEPARATOR)
    is Iterable<*> -> value.joinToString(ARRAY_SEPARATOR)
    is String -> value.replace("\\r\\n", "\r\n")
    else -> value.toString()
  }
}
