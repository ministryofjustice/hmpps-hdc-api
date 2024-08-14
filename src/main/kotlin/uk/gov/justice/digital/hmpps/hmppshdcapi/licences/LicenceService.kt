package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

@Service
class LicenceService(
  private val licenceRepository: LicenceRepository,
  private val prisonApiClient: PrisonApiClient,
  private val objectMapper: ObjectMapper,
) {
  fun getByBookingId(bookingId: Long): HdcLicence? {
    val licence = licenceRepository.findLicenceByBookingId(bookingId).licence

    if (licence.isNullOrEmpty()) {
      return null
    }

    val nomisData = prisonApiClient.getBooking(bookingId)

    val cas2Referral = licence["bassReferral"] as Cas2Referral
    val cas2Requested = objectMapper.convertValue(cas2Referral, Cas2Referral::class.java).bassRequest.bassRequested
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
      curfewAddress = address,
    )
  }

  private fun getAddress(addressObject: Address?): String? {
    if (addressObject === null) {
      return null
    }
    return if (addressObject.addressLine2.isNotEmpty()) {
      "${addressObject.addressLine1}, ${addressObject.addressLine2}, ${addressObject.addressTown}, ${addressObject.postCode}"
    } else {
      "${addressObject.addressLine1}, ${addressObject.addressTown}, ${addressObject.postCode}"
    }
  }
}
