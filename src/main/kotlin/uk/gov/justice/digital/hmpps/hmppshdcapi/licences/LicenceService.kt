package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.slf4j.LoggerFactory
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
      curfewTimes = if (checkForNullValues(curfew?.curfewHours)) curfew?.let { transformToModelCurfewTimes(it.curfewHours) } else null,
    )
  }

  fun checkForNullValues(curfewHours: CurfewHours?): Boolean {
    val missingTimes = mutableListOf<String>()
    if (curfewHours?.mondayFrom == null) {
      missingTimes += "mondayFrom"
    }
    if (curfewHours?.mondayUntil == null) {
      missingTimes += "mondayUntil"
    }
    if (curfewHours?.tuesdayFrom == null) {
      missingTimes += "tuesdayFrom"
    }
    if (curfewHours?.tuesdayUntil == null) {
      missingTimes += "tuesdayUntil"
    }
    if (curfewHours?.wednesdayFrom == null) {
      missingTimes += "wednesdayFrom"
    }
    if (curfewHours?.wednesdayUntil == null) {
      missingTimes += "wednesdayUntil"
    }
    if (curfewHours?.thursdayFrom == null) {
      missingTimes += "thursdayFrom"
    }
    if (curfewHours?.thursdayUntil == null) {
      missingTimes += "thursdayUntil"
    }
    if (curfewHours?.fridayFrom == null) {
      missingTimes += "fridayFrom"
    }
    if (curfewHours?.fridayUntil == null) {
      missingTimes += "fridayUntil"
    }
    if (curfewHours?.saturdayFrom == null) {
      missingTimes += "saturdayFrom"
    }
    if (curfewHours?.saturdayUntil == null) {
      missingTimes += "saturdayUntil"
    }
    if (curfewHours?.sundayFrom == null) {
      missingTimes += "sundayFrom"
    }
    if (curfewHours?.sundayUntil == null) {
      missingTimes += "sundayUntil"
    }

    if (missingTimes.isNotEmpty()) {
      log.info("Missing curfew time(s) for $missingTimes")
      return false
    }
    return true
  }

  fun getAddress(curfew: Curfew?, cas2Referral: Cas2Referral?, proposedAddress: ProposedAddress?): ModelCurfewAddress? {
    val isCurfewApprovedPremisesRequired = curfew?.approvedPremises?.required == Decision.YES
    val isCas2ApprovedPremisesRequired = cas2Referral?.bassAreaCheck?.approvedPremisesRequiredYesNo == Decision.YES
    val isCas2Requested = cas2Referral?.bassRequest?.bassRequested == Decision.YES
    val isCas2Accepted = cas2Referral?.bassOffer?.bassAccepted == OfferAccepted.YES

    val address = when {
      isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired -> curfew?.approvedPremisesAddress
      isCas2ApprovedPremisesRequired -> cas2Referral?.approvedPremisesAddress
      isCas2Requested && isCas2Accepted -> cas2Referral?.bassOffer
      else -> proposedAddress?.curfewAddress
    }
    return address?.let { transformToModelCurfewAddress(it) }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
