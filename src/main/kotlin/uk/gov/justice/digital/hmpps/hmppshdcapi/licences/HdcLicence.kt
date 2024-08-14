package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

data class LicenceData(
  val bassReferral: Cas2Referral,
  val proposedAddress: ProposedAddress,
)

data class Cas2Referral(
  val bassOffer: Address? = null,
  val bassRequest: Cas2Request,
)

data class ProposedAddress(
  val curfewAddress: Address? = null,
)

data class Address(
  val addressLine1: String,
  val addressLine2: String,
  val addressTown: String,
  val postCode: String,
)

data class Cas2Request(
  val bassRequested: String,
)
