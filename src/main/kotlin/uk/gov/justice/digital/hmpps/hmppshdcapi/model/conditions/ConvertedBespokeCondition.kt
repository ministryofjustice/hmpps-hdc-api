package uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Converted bespoke condition")
data class ConvertedBespokeCondition(

  @field:Schema(
    description = "Condition code",
    example = "ABC",
  )
  val code: String?,

  @field:Schema(
    description = "Condition text",
    example = "You must not enter the exclusion zone.",
  )
  val text: String?,
)
