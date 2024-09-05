package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.annotation.JsonProperty

enum class Decision {
  @JsonProperty("Yes")
  YES,

  @JsonProperty("No")
  NO,
}

enum class OfferAccepted {
  @JsonProperty("Yes")
  YES,

  @JsonProperty("Unavailable")
  UNAVAILABLE,

  @JsonProperty("Unsuitable")
  UNSUITABLE,
}

interface Address {
  val addressLine1: String
  val addressLine2: String?
  val addressTown: String
  val postCode: String
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
  val approvedPremisesAddress: CurfewAddress? = null,
  val bassAreaCheck: Cas2AreaCheck? = null,
)

data class ProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: CurfewAddress? = null,
)

data class CurfewAddress(
  override val addressLine1: String,
  override val addressLine2: String? = null,
  override val addressTown: String,

  @JsonProperty("postcode", access = JsonProperty.Access.WRITE_ONLY)
  override val postCode: String,
) : Address

data class Cas2Offer(
  override val addressLine1: String,
  override val addressLine2: String? = null,
  override val addressTown: String,

  @JsonProperty("postcode", access = JsonProperty.Access.WRITE_ONLY)
  override val postCode: String,
  val bassAccepted: OfferAccepted,
) : Address

data class Cas2Request(
  val bassRequested: Decision,
)

data class Curfew(
  val firstNight: FirstNight,
  val curfewHours: CurfewHours,
  val approvedPremisesAddress: CurfewAddress? = null,
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

data class Cas2AreaCheck(
  val approvedPremisesRequiredYesNo: Decision,
)
