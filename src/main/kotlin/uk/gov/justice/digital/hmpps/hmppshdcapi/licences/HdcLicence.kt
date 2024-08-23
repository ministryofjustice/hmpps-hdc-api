package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

enum class Decision {
  Yes,
  No,
}

enum class OfferAccepted {
  Yes,
  Unavailable,
  Unsuitable,
}

data class LicenceData(
  val bassReferral: Cas2Referral,
  val proposedAddress: ProposedAddress,
  val curfew: Curfew,
)

data class Cas2Referral(
  // bassOffer only nullable as address will be either this if Cas2Referral or curfewAddress if proposed address
  val bassOffer: Cas2Offer? = null,
  val bassRequest: Cas2Request,
  val approvedPremisesAddress: Address? = null,
  val bassAreaCheck: BassAreaCheck? = null,
)

data class ProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: Address? = null,
)

data class Address(
  val addressLine1: String,
  val addressLine2: String? = null,
  val addressTown: String,
  val postCode: String,
)

data class Cas2Offer(
  val addressLine1: String,
  val addressLine2: String? = null,
  val addressTown: String,
  val postCode: String,
  val bassAccepted: OfferAccepted,
)

data class Cas2Request(
  val bassRequested: Decision,
)

data class Curfew(
  val firstNight: FirstNight,
  val curfewHours: CurfewHours,
  val approvedPremisesAddress: Address? = null,
  val approvedPremises: ApprovedPremises? = null,
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

data class ApprovedPremises(
  val required: Decision,
)

data class BassAreaCheck(
  val approvedPremisesRequiredYesNo: Decision,
)
