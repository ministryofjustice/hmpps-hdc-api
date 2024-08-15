package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight

@Schema(description = "Describes a HDC Licence")
data class HdcLicence(

  @Schema(description = "The contact telephone number for the prison", example = "0121 123 4567")
  val prisonTelephone: String? = "",

  @Schema(description = "The curfew or CAS2 address for the person", example = "0121 123 4567")
  val curfewAddress: String? = "",

  @Schema(description = "The first night curfew times for the person", example = "")
  val firstNightCurfewHours: FirstNight? = null,

  @Schema(description = "The curfew times for the person following the first night", example = "")
  val curfewHours: CurfewHours? = null,
)
