package uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Batch response containing requested licence IDs and their converted bespoke conditions")
data class ConvertedLicenseBatch(

  @field:Schema(
    description = "List of requested licence IDs",
    example = "[12345, 67890]",
  )
  val ids: List<Long>,

  @field:Schema(
    description = "List of licences with their converted bespoke conditions",
  )
  val conditions: List<ConvertedLicenseConditions>,
)
