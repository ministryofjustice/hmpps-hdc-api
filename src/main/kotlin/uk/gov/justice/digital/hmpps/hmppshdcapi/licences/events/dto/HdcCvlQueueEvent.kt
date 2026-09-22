package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto

import java.time.LocalDateTime

data class HdcCvlQueueEvent(
  val occurredAt: LocalDateTime,
  val licenceId: Long,
  val bookingId: Long,
  val nomsNumber: String,
  val triggeredBy: String,
  val reason: String? = null,
)
