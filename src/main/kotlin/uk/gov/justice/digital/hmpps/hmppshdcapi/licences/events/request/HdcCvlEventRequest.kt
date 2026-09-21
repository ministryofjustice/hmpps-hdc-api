package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.request

import uk.gov.justice.digital.hmpps.hmppshdcapi.util.HdcCvlEventType

data class HdcCvlEventRequest(
  val eventType: HdcCvlEventType,
  val licenceId: Long,
  val bookingId: Long,
  val nomsNumber: String,
  val triggeredBy: String,
  val reason: String? = null,
)
