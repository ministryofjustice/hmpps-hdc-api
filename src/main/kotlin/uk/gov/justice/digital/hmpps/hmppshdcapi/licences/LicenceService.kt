package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

@Service
class LicenceService(
  private val licenceRepository: LicenceRepository,
  private val objectMapper: ObjectMapper,
) {
  fun getByBookingId(bookingId: Long): HdcLicence? {
    val licenceObject = licenceRepository.findLicenceByBookingId(bookingId) ?: return null

    val licence = licenceObject.licence

    if (licence.isNullOrEmpty()) {
      throw EntityNotFoundException("Licence data not found for booking $bookingId")
    }

    val cas2ReferralObject = licence["bassReferral"]
    val cas2Referral = objectMapper.convertValue(cas2ReferralObject, Cas2Referral::class.java)

    val curfewObject = licence["curfew"]
    val curfew = objectMapper.convertValue(curfewObject, Curfew::class.java)

    val proposedAddressObject = licence["proposedAddress"]
    val proposedAddress = objectMapper.convertValue(proposedAddressObject, ProposedAddress::class.java)

    return HdcLicence(
      curfewAddress = getAddress(curfew, cas2Referral, proposedAddress),
      firstNightCurfewHours = curfew.firstNight,
      curfewHours = curfew.curfewHours,
    )
  }

  fun getAddress(curfew: Curfew, cas2Referral: Cas2Referral, proposedAddress: ProposedAddress): CurfewAddress {
    val isCurfewApprovedPremisesRequired = curfew.approvedPremises?.required == Decision.YES
    val isCas2ApprovedPremisesRequired = cas2Referral.bassAreaCheck?.approvedPremisesRequiredYesNo == Decision.YES
    val isCas2Requested = cas2Referral.bassRequest.bassRequested == Decision.YES
    val isCas2Accepted = cas2Referral.bassOffer?.bassAccepted == OfferAccepted.YES

    if (isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired) {
      return formatAddress(curfew.approvedPremisesAddress!!)
    }

    if (isCas2ApprovedPremisesRequired) {
      return formatAddress(cas2Referral.approvedPremisesAddress!!)
    }

    if (isCas2Requested && isCas2Accepted) {
      val cas2Address = cas2Referral.bassOffer!!
      return formatAddress(cas2Address)
    }
    return formatAddress(proposedAddress.curfewAddress!!)
  }

  private fun formatAddress(addressObject: Address): CurfewAddress =
    CurfewAddress(
      addressLine1 = addressObject.addressLine1,
      addressLine2 = addressObject.addressLine2,
      addressTown = addressObject.addressTown,
      postcode = addressObject.postcode,
    )
}
