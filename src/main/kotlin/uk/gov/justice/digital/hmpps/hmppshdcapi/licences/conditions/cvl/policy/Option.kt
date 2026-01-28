package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy

data class Option(
  val value: String,
  val conditional: Conditional? = null,
)
