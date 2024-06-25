package uk.gov.justice.digital.hmpps.hmppshdcapi.util

enum class AuditEventType(val eventType: String) {
  SYSTEM_API("SYSTEM:API"),
  SYSTEM_JOB("SYSTEM:JOB"),
  SYSTEM_MIGRATION("SYSTEM:MIGRATION"),
}
