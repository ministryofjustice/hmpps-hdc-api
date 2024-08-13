package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison

data class PrisonContactDetails(
  val agencyId: String,
  val phones: Telephone
)

data class Telephone(
  val number: String,
  val type: String
)