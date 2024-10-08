package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

@Service
class LicenceService(
  private val licenceRepository: LicenceRepository,
) {
  fun getByBookingId(bookingId: Long): HdcLicence? {
    val licences = licenceRepository.findByBookingIds(listOf(bookingId))

    if (licences.isEmpty()) {
      throw NoDataFoundException("licence", "booking id", bookingId)
    }

    val licenceData = licences.first().licence ?: throw NoDataFoundException("licence data", "booking id", bookingId)

    val cas2Referral = licenceData.bassReferral
    val curfew = licenceData.curfew
    val proposedAddress = licenceData.proposedAddress

    return HdcLicence(
      curfewAddress = getAddress(curfew, cas2Referral, proposedAddress),
      firstNightCurfewHours = curfew?.firstNight,
      curfewHours = curfew?.curfewHours,
    )
  }

  fun getAddress(curfew: Curfew?, cas2Referral: Cas2Referral?, proposedAddress: ProposedAddress?): CurfewAddress {
    val isCurfewApprovedPremisesRequired = curfew?.approvedPremises?.required == Decision.YES
    val isCas2ApprovedPremisesRequired = cas2Referral?.bassAreaCheck?.approvedPremisesRequiredYesNo == Decision.YES
    val isCas2Requested = cas2Referral?.bassRequest?.bassRequested == Decision.YES
    val isCas2Accepted = cas2Referral?.bassOffer?.bassAccepted == OfferAccepted.YES

    if (isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired) {
      return formatAddress(curfew?.approvedPremisesAddress!!)
    }

    if (isCas2ApprovedPremisesRequired) {
      return formatAddress(cas2Referral?.approvedPremisesAddress!!)
    }

    if (isCas2Requested && isCas2Accepted) {
      val cas2Address = cas2Referral?.bassOffer!!
      return formatAddress(cas2Address)
    }
    return formatAddress(proposedAddress?.curfewAddress!!)
  }

  private fun formatAddress(addressObject: Address): CurfewAddress =
    CurfewAddress(
      addressLine1 = addressObject.addressLine1,
      addressLine2 = addressObject.addressLine2,
      addressTown = addressObject.addressTown,
      postCode = addressObject.postCode,
    )
}
