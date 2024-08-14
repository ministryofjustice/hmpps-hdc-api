package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes a HDC Licence")
data class HdcLicence(

  @Schema(description = "The contact telephone number for the prison", example = "0121 123 4567")
  val prisonTelephone: String? = "",

  @Schema(description = "The curfew or CAS2 address for the person", example = "0121 123 4567")
  val curfewAddress: String? = "",
)
