package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
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

    val curfewObject = licence["curfew"]
    val curfew = objectMapper.convertValue(curfewObject, Curfew::class.java)
    val approvedPremisesDecision = curfew.approvedPremises.required
    val isCurfewApprovedPremisesRequired = approvedPremisesDecision == Decision.Yes

    val isCas2ApprovedPremisesRequired = cas2Referral.bassAreaCheck.approvedPremisesRequiredYesNo == Decision.Yes

    // - Split selection of address into separate function
    // - Unit tests to include the two new approved premise scenarios
    // - Update unit test to include test for new bassAccepted and change in bassRequested being a decision rather than a string
    // - Integration test data - add extra licences to the sql file which show approved premise licences
    // - Add integration tests for those licences

    var formattedAddress: String?

    if (isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired) {
      formattedAddress = getAddress(curfew.approvedPremisesAddress)
    }

    if (isCas2ApprovedPremisesRequired) {
      formattedAddress = getAddress(cas2Referral.approvedPremisesAddress as Address)
    }

    val cas2Requested = cas2Referral.bassRequest.bassRequested
    val cas2Accepted = cas2Referral.bassOffer?.bassAccepted

    if (cas2Requested == Decision.Yes && cas2Accepted == OfferAccepted.Yes) {
      val cas2Address = cas2Referral.bassOffer
      val address = Address(
        addressLine1 = cas2Address.addressLine1,
        addressLine2 = cas2Address.addressLine2,
        addressTown = cas2Address.addressTown,
        postCode = cas2Address.postCode,
      )
      formattedAddress = getAddress(address)
    } else {
      val proposedAddress = licence["proposedAddress"]
      val curfewAddress = objectMapper.convertValue(proposedAddress, ProposedAddress::class.java).curfewAddress as Address
      formattedAddress = getAddress(curfewAddress)
    }

    val firstNightHours = objectMapper.convertValue(curfew, Curfew::class.java).firstNight
    val curfewHours = objectMapper.convertValue(curfew, Curfew::class.java).curfewHours

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
