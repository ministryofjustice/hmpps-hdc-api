package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

// NOTE: DO NOT EDIT DIRECTLY: This is generated via "npm run generate:kotlin-types" in https://github.com/ministryofjustice/licences

data class ConditionMetadata(
  val id: String,
  val text: String,
  val userInput: String,
  val fieldPosition: Map<String, Int>,
  val groupName: String,
  val subgroupName: String?,
)
