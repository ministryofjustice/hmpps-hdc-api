package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

data class Cas2Request(
  val bassRequest: BassRequest
)

data class BassRequest(
  val bassRequested: String
)

data class BassReferral(
  val bassOffer: Address
)

data class Address(
  val addressLine1: String,
  val addressLine2: String,
  val addressTown: String,
  val postCode: String,
)

@Service
class LicenceService(
  val licenceRepository: LicenceRepository,
  val prisonApiClient: PrisonApiClient,
  val objectMapper: ObjectMapper,
) {
  fun getByBookingId(bookingId: Long): HdcLicence? {
    val licence = licenceRepository.findLicenceByBookingId(bookingId).licence

    if (licence.isNullOrEmpty()) {
      return null
    }

    val nomisData = prisonApiClient.getBooking(bookingId)

    val cas2Referral = licence["bassReferral"] as BassReferral
    val cas2Requested = objectMapper.convertValue(cas2Referral, Cas2Request::class.java).bassRequest.bassRequested
    val address: String?
    if (cas2Requested === "Yes") {
      val cas2Address = cas2Referral.bassOffer
      address = getAddress(cas2Address)
    } else {
      val curfewAddress = licence["curfewAddress"] as Address
      address = getAddress(curfewAddress)
    }

    val prisonContactDetails = prisonApiClient.getPrisonContactDetails(nomisData?.agencyId)
    val telephoneNumber = prisonContactDetails?.phones?.find { it.type === "BUS" }

    return HdcLicence(
      prisonTelephone = telephoneNumber?.number,
      curfewAddress = address
    )

  }

  private fun getAddress(addressObject: Address): String {
    return if (addressObject.addressLine2.isNotEmpty()) {
      "${addressObject.addressLine1}, ${addressObject.addressLine2}, ${addressObject.addressTown}, ${addressObject.postCode}"
    } else {
      "${addressObject.addressLine1}, ${addressObject.addressTown}, ${addressObject.postCode}"
    }
  }
}





}
