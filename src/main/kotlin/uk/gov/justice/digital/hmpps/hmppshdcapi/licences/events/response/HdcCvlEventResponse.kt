package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.response

import java.time.LocalDateTime

data class HdcCvlEventResponse(
  val eventType: String,
  val occurredAt: LocalDateTime,
)
