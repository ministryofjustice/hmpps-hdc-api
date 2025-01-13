package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalTime

@Schema(description = "Describes the first night curfew times on a HDC Licence")
data class FirstNight(

  @Schema(description = "The starting time for the curfew on the first night", example = "19:00")
  @JsonFormat(pattern = "HH:mm")
  val firstNightFrom: LocalTime? = null,

  @Schema(description = "The ending time for the curfew on the first night", example = "08:00")
  @JsonFormat(pattern = "HH:mm")
  val firstNightUntil: LocalTime? = null,
)
