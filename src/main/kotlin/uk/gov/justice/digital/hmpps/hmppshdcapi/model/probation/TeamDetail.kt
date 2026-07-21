package uk.gov.justice.digital.hmpps.hmppshdcapi.model.probation

data class TeamDetail(
  val code: String,
  val description: String,
  val borough: Detail,
  val district: Detail,
  val provider: Detail,
)
