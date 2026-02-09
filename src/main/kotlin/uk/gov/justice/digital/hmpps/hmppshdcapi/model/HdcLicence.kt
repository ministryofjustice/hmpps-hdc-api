package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatus

@Schema(description = "Describes a HDC Licence")
data class HdcLicence(
  @field:Schema(description = "The id for the licence")
  val licenceId: Long? = null,

  @field:Schema(description = "The curfew or CAS2 address for the person")
  val curfewAddress: CurfewAddress? = null,

  @field:Schema(description = "The first night curfew times for the person")
  val firstNightCurfewHours: FirstNight? = null,

  @field:Schema(description = "The curfew times for the person following the first night")
  val curfewTimes: List<CurfewTimes>? = null,

  @field:Schema(description = "The HDC status for the person")
  val status: HdcStatus,
)
