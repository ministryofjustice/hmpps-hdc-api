package uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Converted licence with bespoke conditions")
data class ConvertedLicenseConditions(

  @field:Schema(description = "Licence identifier", example = "12345")
  val id: Long,

  @field:Schema(description = "Prison number associated with the licence", example = "A1234BC")
  var prisonNumber: String,

  @field:Schema(description = "Booking identifier", example = "987654")
  val bookingId: Long,

  @field:Schema(description = "List of converted bespoke conditions")
  val conditions: List<ConvertedBespokeCondition>,
)
