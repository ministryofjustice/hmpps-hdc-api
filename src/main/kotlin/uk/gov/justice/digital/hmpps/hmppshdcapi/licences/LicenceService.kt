package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.transformToModelCurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.transformToModelCurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.transformToModelFirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewAddress as ModelCurfewAddress

@Service
class LicenceService(
  private val licenceRepository: LicenceRepository,
) {
  fun getByBookingId(bookingId: Long): HdcLicence? {
    val licences = licenceRepository.findByBookingIds(listOf(bookingId))

    if (licences.isEmpty()) {
      throw NoDataFoundException("licence", "booking id", bookingId)
    }

    val licence = licences.first()

    val licenceData = licence.licence ?: throw NoDataFoundException("licence data", "booking id", bookingId)

    val cas2Referral = licenceData.bassReferral
    val curfew = licenceData.curfew
    val proposedAddress = licenceData.proposedAddress

    return HdcLicence(
      licenceId = licence.id,
      curfewAddress = getAddress(curfew, cas2Referral, proposedAddress),
      firstNightCurfewHours = curfew?.firstNight?.let { transformToModelFirstNight(it) },
      // curfewHours referred to as curfewTimes in CVL as going forward a more suitable name and had to distinguish between the two different curfew data formats
      curfewTimes = curfew?.let { transformToModelCurfewTimes(it.curfewHours) },
    )
  }

  fun getAddress(curfew: Curfew?, cas2Referral: Cas2Referral?, proposedAddress: ProposedAddress?): ModelCurfewAddress? {
    val isCurfewApprovedPremisesRequired = curfew?.approvedPremises?.required == Decision.YES
    val isCas2ApprovedPremisesRequired = cas2Referral?.bassAreaCheck?.approvedPremisesRequiredYesNo == Decision.YES
    val isCas2Requested = cas2Referral?.bassRequest?.bassRequested == Decision.YES
    val isCas2Accepted = cas2Referral?.bassOffer?.bassAccepted == OfferAccepted.YES

    val address = when {
      isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired -> curfew.approvedPremisesAddress
      isCas2ApprovedPremisesRequired -> cas2Referral.approvedPremisesAddress
      isCas2Requested && isCas2Accepted -> cas2Referral.bassOffer
      else -> proposedAddress?.curfewAddress
    }
    return address?.let { transformToModelCurfewAddress(it) }
  }
}
