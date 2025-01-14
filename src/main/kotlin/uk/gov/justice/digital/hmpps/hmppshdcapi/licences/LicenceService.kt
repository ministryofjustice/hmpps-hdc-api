package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalTime

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

    return curfew?.curfewHours?.let { formatCurfewHoursObject(curfew.curfewHours) }?.let {
      HdcLicence(
        licenceId = licence.id,
        curfewAddress = getAddress(curfew, cas2Referral, proposedAddress),
        firstNightCurfewHours = curfew.firstNight,
        // curfewHours referred to as curfewTimes in CVL as going forward a more suitable name and had to distinguish between the two different curfew data formats
        curfewTimes = it,
      )
    }
  }

  fun getAddress(curfew: Curfew?, cas2Referral: Cas2Referral?, proposedAddress: ProposedAddress?): CurfewAddress? {
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

    return address?.let { formatAddress(it) }
  }

  private fun formatAddress(addressObject: Address): CurfewAddress =
    CurfewAddress(
      addressLine1 = addressObject.addressLine1,
      addressLine2 = addressObject.addressLine2,
      addressTown = addressObject.addressTown,
      postCode = addressObject.postCode,
    )

  private fun formatCurfewHoursObject(curfewHours: CurfewHours): List<CurfewTimes> {
    return listOf(
      CurfewTimes(
        fromDay = MONDAY,
        fromTime = LocalTime.parse(curfewHours.mondayFrom),
        untilDay = TUESDAY,
        untilTime = LocalTime.parse(curfewHours.tuesdayUntil),
      ),
      CurfewTimes(
        fromDay = TUESDAY,
        fromTime = LocalTime.parse(curfewHours.tuesdayFrom),
        untilDay = WEDNESDAY,
        untilTime = LocalTime.parse(curfewHours.wednesdayUntil),
      ),
      CurfewTimes(
        fromDay = WEDNESDAY,
        fromTime = LocalTime.parse(curfewHours.wednesdayFrom),
        untilDay = THURSDAY,
        untilTime = LocalTime.parse(curfewHours.thursdayUntil),
      ),
      CurfewTimes(
        fromDay = THURSDAY,
        fromTime = LocalTime.parse(curfewHours.thursdayFrom),
        untilDay = FRIDAY,
        untilTime = LocalTime.parse(curfewHours.fridayUntil),
      ),
      CurfewTimes(
        fromDay = FRIDAY,
        fromTime = LocalTime.parse(curfewHours.fridayFrom),
        untilDay = SATURDAY,
        untilTime = LocalTime.parse(curfewHours.saturdayUntil),
      ),
      CurfewTimes(
        fromDay = SATURDAY,
        fromTime = LocalTime.parse(curfewHours.saturdayFrom),
        untilDay = SUNDAY,
        untilTime = LocalTime.parse(curfewHours.sundayUntil),
      ),
      CurfewTimes(
        fromDay = SUNDAY,
        fromTime = LocalTime.parse(curfewHours.sundayFrom),
        untilDay = MONDAY,
        untilTime = LocalTime.parse(curfewHours.mondayUntil),
      ),
    )
  }
}
