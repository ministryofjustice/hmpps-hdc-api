package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

data class LicenceData(
  val bassReferral: Cas2Referral,
  val proposedAddress: ProposedAddress,
  val curfew: Curfew,
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

data class Curfew(
  val firstNight: FirstNight,
  val curfewHours: CurfewHours,
)

data class FirstNight(
  val firstNightFrom: String,
  val firstNightUntil: String,
)

data class CurfewHours(
  val mondayFrom: String,
  val mondayUntil: String,
  val tuesdayFrom: String,
  val tuesdayUntil: String,
  val wednesdayFrom: String,
  val wednesdayUntil: String,
  val thursdayFrom: String,
  val thursdayUntil: String,
  val fridayFrom: String,
  val fridayUntil: String,
  val saturdayFrom: String,
  val saturdayUntil: String,
  val sundayFrom: String,
  val sundayUntil: String,
)
