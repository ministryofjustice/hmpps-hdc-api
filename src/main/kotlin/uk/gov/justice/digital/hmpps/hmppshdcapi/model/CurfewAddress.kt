package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes the curfew address on a HDC licence")
data class CurfewAddress(

  @Schema(description = "The first line of the curfew address", example = "1 Some Street")
  val addressLine1: String? = null,

  @Schema(description = "The second line of the curfew address", example = "Off Some Road")
  val addressLine2: String? = null,

  @Schema(description = "The town or city associated with the curfew address", example = "Some Town")
  val townOrCity: String? = null,

  @Schema(description = "The county associated with the curfew address", example = "Some County")
  val county: String? = null,

  @Schema(description = "The postcode for the curfew address", example = "SO30 2UH")
  val postcode: String? = null,

  @Schema(description = "The curfew address type for the person")
  val curfewAddressType: AddressType? = null,
)
