package uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

private const val maxBatchSize = 1000

@Schema(description = "Request containing licence IDs to retrieve bespoke conditions for")
data class LicenceIdBatchRequest(

  @field:NotEmpty
  @field:Size(max = maxBatchSize, message = "Maximum $maxBatchSize licence IDs allowed per request")
  @field:Schema(
    description = "List of licence IDs",
    example = "[1, 2, 3]",
  )
  val ids: List<Long>,
)
