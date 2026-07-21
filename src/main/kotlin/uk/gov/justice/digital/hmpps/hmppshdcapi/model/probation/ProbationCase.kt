package uk.gov.justice.digital.hmpps.hmppshdcapi.model.probation

data class ProbationCase(
  val crn: String,
  val nomisId: String? = null,
  val croNumber: String? = null,
  val pncNumber: String? = null,
)
