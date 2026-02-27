package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.cvl.policy

data class Conditional(
  val inputs: List<ConditionalInput>,
) : HasInputs {
  override fun getConditionInputs() = inputs.map { it.toInput() }
}
