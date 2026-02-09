package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.attemptToGuessVersion
import kotlin.collections.associateBy

object LicenceConditionRenderer {

  private val conditionFieldMerger = LicenceConditionFieldMerger()

  fun renderConditions(licenceData: LicenceData?, conditionVersion: Int? = null): List<Pair<String, String>> {
    val licenceConditions = licenceData?.licenceConditions ?: return emptyList()
    val additionalData = licenceConditions.additional ?: return emptyList()

    val conditionVersion = conditionVersion ?: attemptToGuessVersion(licenceData.licenceConditions.additional)
    val conditionTemplate = getConditionTemplateVersion(conditionVersion)

    return additionalData.mapNotNull { (conditionId, additionalFields) ->
      val conditionMeta = conditionTemplate[conditionId] ?: return@mapNotNull null
      conditionId to renderCondition(conditionMeta, additionalFields)
    }
  }

  fun renderConditions(licence: Licence, conditionVersion: Int? = null): List<Pair<String, String>> = renderConditions(licence.licence, conditionVersion)

  fun renderCondition(
    conditionMeta: ConditionMetadata,
    additionalFields: Map<String, Any>,
  ): String {
    val condensedFields = conditionFieldMerger.mergeIfRequired(conditionMeta, additionalFields)
    val cleanedConditionMetaData = removeUnwantedFieldsFromMetaDataWhenRequired(conditionMeta, condensedFields)
    val orderedFieldsData = getFieldsDataInCorrectPositions(cleanedConditionMetaData, condensedFields)
    val renderedText = renderDataToText(conditionMeta.text, orderedFieldsData)
    return renderedText
  }

  fun renderDataToText(
    textTemplate: String,
    fieldsData: List<Any>,
  ): String {
    var rendered = textTemplate
    fieldsData.forEach { value ->
      rendered = rendered.replaceFirst(Regex("\\[.*?]"), convertToString(value))
    }
    return rendered
  }

  private fun removeUnwantedFieldsFromMetaDataWhenRequired(
    conditionMetaData: ConditionMetadata,
    additionalFields: Map<String, Any>,
  ): ConditionMetadata {
    var inProcessConditionMetaData = conditionMetaData

    // This is a bit of a hack between version 1 and 2 of the condition meta data (see version info)
    // This will have to change if we have a different version in the future
    when (inProcessConditionMetaData.id) {
      "REPORTTO", "REPORT_TO" -> {
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
    is Array<*> -> value.joinToString(", ")
    is Iterable<*> -> value.joinToString(", ")
    else -> value.toString()
  }
}

// batch comparison util on front end ,.. open end point for text convert
