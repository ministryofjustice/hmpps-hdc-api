package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import java.time.LocalTime

@Schema(description = "Describes the curfew times on a HDC licence")
data class CurfewTimes(

  @field:Schema(description = "The starting day for the curfew time", example = "MONDAY")
  val fromDay: DayOfWeek,

  @field:Schema(description = "The starting time for the curfew", example = "15:00")
  @field:JsonFormat(pattern = "HH:mm")
  val fromTime: LocalTime? = null,

  @field:Schema(description = "The ending day for the curfew time", example = "TUESDAY")
  val untilDay: DayOfWeek,

  @field:Schema(description = "The ending time for the curfew", example = "07:00")
  @field:JsonFormat(pattern = "HH:mm")
  val untilTime: LocalTime? = null,

)
