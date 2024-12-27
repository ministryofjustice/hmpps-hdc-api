package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes a HDC Licence")
data class HdcLicence(
  @Schema(description = "The id for the licence")
  val licenceId: Long? = null,

  @Schema(description = "The curfew or CAS2 address for the person")
  val curfewAddress: CurfewAddress? = null,

  @Schema(description = "The first night curfew times for the person")
  val firstNightCurfewHours: FirstNight? = null,

  @Schema(description = "The curfew times for the person following the first night")
  val curfewTimes: List<CurfewTimes>,
)
