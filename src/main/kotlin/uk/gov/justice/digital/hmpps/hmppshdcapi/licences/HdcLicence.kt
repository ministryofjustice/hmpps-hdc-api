package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
  val bassReferral: Cas2Referral?,
  val proposedAddress: ProposedAddress?,
  val curfew: Curfew?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Referral(
  // bassOffer only nullable as address will be either this if Cas2Referral or curfewAddress if proposed address
  val bassOffer: Cas2Offer? = null,
  val bassRequest: Cas2Request,
  val approvedPremisesAddress: CurfewAddress? = null,
  val bassAreaCheck: Cas2AreaCheck? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProposedAddress(
  // curfewAddress only nullable as address will be either this if ProposedAddress or bassOffer if cas2
  val curfewAddress: CurfewAddress? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurfewAddress(
  override val addressLine1: String,
  override val addressLine2: String? = null,
  override val addressTown: String,
  override val postCode: String,
) : Address

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Offer(
  override val addressLine1: String,
  override val addressLine2: String? = null,
  override val addressTown: String,
  override val postCode: String,
  val bassAccepted: OfferAccepted,
) : Address

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2Request(
  val bassRequested: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Curfew(
  val firstNight: FirstNight,
  val curfewHours: CurfewHours,
  val approvedPremisesAddress: CurfewAddress? = null,
  val approvedPremises: ApprovedPremises? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FirstNight(
  val firstNightFrom: String,
  val firstNightUntil: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApprovedPremises(
  val required: Decision,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Cas2AreaCheck(
  val approvedPremisesRequiredYesNo: Decision,
)
