package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight

@Schema(description = "Describes a HDC Licence")
data class HdcLicence(

  @Schema(description = "The curfew or CAS2 address for the person")
  val curfewAddress: CurfewAddress? = null,

  @Schema(description = "The first night curfew times for the person")
  val firstNightCurfewHours: FirstNight? = null,

  @Schema(description = "The curfew times for the person following the first night")
  val curfewHours: CurfewHours? = null,
)
