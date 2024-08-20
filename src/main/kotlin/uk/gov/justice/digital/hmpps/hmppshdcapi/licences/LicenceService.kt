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
    val licenceObject = licenceRepository.findLicenceByBookingId(bookingId) ?: return null

    val licence = licenceObject.licence

    if (licence.isNullOrEmpty()) {
      return null
    }

    val nomisData = prisonApiClient.getBooking(bookingId)

    val cas2ReferralObject = licence["bassReferral"]
    val cas2Referral = objectMapper.convertValue(cas2ReferralObject, Cas2Referral::class.java)
    val cas2Requested = cas2Referral.bassRequest.bassRequested

    val formattedAddress: String?
    if (cas2Requested == "Yes") {
      val cas2Address = cas2Referral.bassOffer as Address
      formattedAddress = getAddress(cas2Address)
    } else {
      val proposedAddress = licence["proposedAddress"]
      val curfewAddress = objectMapper.convertValue(proposedAddress, ProposedAddress::class.java).curfewAddress as Address
      formattedAddress = getAddress(curfewAddress)
    }

    val curfewTimes = licence["curfew"]
    val firstNightHours = objectMapper.convertValue(curfewTimes, Curfew::class.java).firstNight
    val curfewHours = objectMapper.convertValue(curfewTimes, Curfew::class.java).curfewHours

    val prisonContactDetails = prisonApiClient.getPrisonContactDetails(nomisData?.agencyId)
    val telephoneNumber = prisonContactDetails?.phones?.find { it.type == "BUS" }
    return HdcLicence(
      prisonTelephone = telephoneNumber?.number,
      curfewAddress = formattedAddress,
      firstNightCurfewHours = firstNightHours,
      curfewHours = curfewHours,
    )
  }

  private fun getAddress(addressObject: Address): String = if (addressObject.addressLine2 != null) {
    "${addressObject.addressLine1}, ${addressObject.addressLine2}, ${addressObject.addressTown}, ${addressObject.postCode}"
  } else {
    "${addressObject.addressLine1}, ${addressObject.addressTown}, ${addressObject.postCode}"
  }
}
