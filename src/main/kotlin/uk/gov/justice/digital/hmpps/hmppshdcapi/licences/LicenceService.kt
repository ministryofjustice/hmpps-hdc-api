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

    val cas2ReferralObject = licence["bassReferral"]
    val cas2Referral = objectMapper.convertValue(cas2ReferralObject, Cas2Referral::class.java)

    val curfewObject = licence["curfew"]
    val curfew = objectMapper.convertValue(curfewObject, Curfew::class.java)

    val proposedAddressObject = licence["proposedAddress"]
    val proposedAddress = objectMapper.convertValue(proposedAddressObject, ProposedAddress::class.java)

    val nomisData = prisonApiClient.getBooking(bookingId)
    val prisonContactDetails = prisonApiClient.getPrisonContactDetails(nomisData?.agencyId)
    val telephoneNumber = prisonContactDetails?.phones?.find { it.type == "BUS" }

    return HdcLicence(
      prisonTelephone = telephoneNumber?.number,
      curfewAddress = getAddress(curfew, cas2Referral, proposedAddress),
      firstNightCurfewHours = curfew.firstNight,
      curfewHours = curfew.curfewHours,
    )

    // - Unit tests to include the two new approved premise scenarios
    // - Update unit test to include test for new bassAccepted and change in bassRequested being a decision rather than a string
    // - Integration test data - add extra licences to the sql file which show approved premise licences
    // - Add integration tests for those licences
  }

  fun getAddress(curfew: Curfew, cas2Referral: Cas2Referral, proposedAddress: ProposedAddress): String {
    val isCurfewApprovedPremisesRequired = curfew.approvedPremises?.required == Decision.Yes
    val isCas2ApprovedPremisesRequired = cas2Referral.bassAreaCheck?.approvedPremisesRequiredYesNo == Decision.Yes
    val isCas2Requested = cas2Referral.bassRequest.bassRequested == Decision.Yes
    val isCas2Accepted = cas2Referral.bassOffer?.bassAccepted == OfferAccepted.Yes

    if (isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired) {
      println("In here")
      return formatAddress(curfew.approvedPremisesAddress!!)
    }

    if (isCas2ApprovedPremisesRequired) {
      return formatAddress(cas2Referral.approvedPremisesAddress!!)
    }

    if (isCas2Requested && isCas2Accepted) {
      val cas2Address = cas2Referral.bassOffer as Cas2Offer
      val address = Address(
        addressLine1 = cas2Address.addressLine1,
        addressLine2 = cas2Address.addressLine2,
        addressTown = cas2Address.addressTown,
        postCode = cas2Address.postCode,
      )
      return formatAddress(address)
    }

    return formatAddress(proposedAddress.curfewAddress!!)
  }

  private fun formatAddress(addressObject: Address): String = if (addressObject.addressLine2 != null) {
    "${addressObject.addressLine1}, ${addressObject.addressLine2}, ${addressObject.addressTown}, ${addressObject.postCode}"
  } else {
    "${addressObject.addressLine1}, ${addressObject.addressTown}, ${addressObject.postCode}"
  }
}
