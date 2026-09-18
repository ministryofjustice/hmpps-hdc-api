package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.HdcCvlEventType

data class HdcCvlEventRequest(
  @field:NotNull(message = "eventType must be supplied")
  var eventType: HdcCvlEventType? = null,
  @field:NotNull(message = "licenceId must be supplied")
  var licenceId: Long? = null,
  @field:NotNull(message = "bookingId must be supplied")
  var bookingId: Long? = null,
  @field:NotBlank(message = "nomsNumber must be supplied")
  var nomsNumber: String? = null,
  @field:NotBlank(message = "triggeredBy must be supplied")
  var triggeredBy: String? = null,
  var reason: String? = null,
)
