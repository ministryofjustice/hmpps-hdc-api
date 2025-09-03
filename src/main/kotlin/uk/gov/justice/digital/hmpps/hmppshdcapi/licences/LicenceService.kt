package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.AddressType
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
    val curfewHours = curfew?.curfewHours

    val missingCurfewTimes = curfewHours.getNullTimes()
    val curfewTimes = if (missingCurfewTimes.isNotEmpty()) {
      log.info("Missing curfew time(s) for $missingCurfewTimes")
      null
    } else {
      curfewHours?.let {
        transformToModelCurfewTimes(it)
      }
    }

    return HdcLicence(
      licenceId = licence.id,
      curfewAddress = getAddress(curfew, cas2Referral, proposedAddress, licence.id),
      firstNightCurfewHours = curfew?.firstNight?.let { transformToModelFirstNight(it) },
      // curfewHours referred to as curfewTimes in CVL as going forward a more suitable name and had to distinguish between the two different curfew data formats
      curfewTimes = curfewTimes,
    )
  }

  fun CurfewHours?.getNullTimes(): List<String> {
    if (this == null) {
      return listOf("all times")
    }
    val missingTimes = mutableListOf<String>()
    if (mondayFrom == null) {
      missingTimes += "mondayFrom"
    }
    if (mondayUntil == null) {
      missingTimes += "mondayUntil"
    }
    if (tuesdayFrom == null) {
      missingTimes += "tuesdayFrom"
    }
    if (tuesdayUntil == null) {
      missingTimes += "tuesdayUntil"
    }
    if (wednesdayFrom == null) {
      missingTimes += "wednesdayFrom"
    }
    if (wednesdayUntil == null) {
      missingTimes += "wednesdayUntil"
    }
    if (thursdayFrom == null) {
      missingTimes += "thursdayFrom"
    }
    if (thursdayUntil == null) {
      missingTimes += "thursdayUntil"
    }
    if (fridayFrom == null) {
      missingTimes += "fridayFrom"
    }
    if (fridayUntil == null) {
      missingTimes += "fridayUntil"
    }
    if (saturdayFrom == null) {
      missingTimes += "saturdayFrom"
    }
    if (saturdayUntil == null) {
      missingTimes += "saturdayUntil"
    }
    if (sundayFrom == null) {
      missingTimes += "sundayFrom"
    }
    if (sundayUntil == null) {
      missingTimes += "sundayUntil"
    }
    return missingTimes
  }

  fun getAddress(curfew: Curfew?, cas2Referral: CurrentCas2Referral?, proposedAddress: ProposedAddress?, licenceId: Long?): ModelCurfewAddress? {
    val isCurfewApprovedPremisesRequired = curfew?.approvedPremises?.required == Decision.YES
    val isCas2ApprovedPremisesRequired = cas2Referral?.bassAreaCheck?.approvedPremisesRequiredYesNo == Decision.YES
    val isCas2Requested = cas2Referral?.bassRequest?.bassRequested == Decision.YES
    val isCas2Accepted = cas2Referral?.bassOffer?.bassAccepted == OfferAccepted.YES

    val address = when {
      isCurfewApprovedPremisesRequired && !isCas2ApprovedPremisesRequired -> curfew?.let { it.approvedPremisesAddress to AddressType.CAS }
      isCas2ApprovedPremisesRequired -> cas2Referral?.let { it.approvedPremisesAddress to AddressType.CAS }
      isCas2Requested && isCas2Accepted -> cas2Referral?.let { it.bassOffer to AddressType.CAS }
      else -> proposedAddress?.let { it.curfewAddress to AddressType.RESIDENTIAL }
    }

    if (address?.first == null) {
      log.info("Missing curfew address for $licenceId")
      return null
    }

    val missingAddressFields = address.first!!.getMissingAddressFields()

    return if (missingAddressFields.isNotEmpty()) {
      log.info("Missing $missingAddressFields address field(s) for licence $licenceId")
      null
    } else {
      transformToModelCurfewAddress(address)
    }
  }

  fun Address.getMissingAddressFields(): List<String> {
    val missingFields = mutableListOf<String>()
    if (addressLine1.isNullOrBlank()) {
      missingFields += "addressLine1"
    }
    if (addressTown.isNullOrBlank()) {
      missingFields += "addressTown"
    }
    if (postCode.isNullOrBlank()) {
      missingFields += "postCode"
    }
    return missingFields
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
