package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.request

import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.HdcCvlEventType

data class HdcCvlEventRequest(
  val eventType: HdcCvlEventType,
  val licenceId: Long,
  val bookingId: Long,
  @field:NotBlank(message = "nomsNumber must be supplied")
  val nomsNumber: String? = null,
  @field:NotBlank(message = "triggeredBy must be supplied")
  val triggeredBy: String? = null,
  val reason: String? = null,
)
