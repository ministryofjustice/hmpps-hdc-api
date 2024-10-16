package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
import java.time.DayOfWeek
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

  private fun formatCurfewHoursObject(curfewHours: CurfewHours): List<CurfewTimes> {
    val day1 =
      CurfewTimes(
        fromDay = DayOfWeek.MONDAY,
        fromTime = LocalTime.parse(curfewHours.mondayFrom),
        untilDay = DayOfWeek.TUESDAY,
        untilTime = LocalTime.parse(curfewHours.tuesdayUntil),
      )
    val day2 =
      CurfewTimes(
        fromDay = DayOfWeek.TUESDAY,
        fromTime = LocalTime.parse(curfewHours.tuesdayFrom),
        untilDay = DayOfWeek.WEDNESDAY,
        untilTime = LocalTime.parse(curfewHours.wednesdayUntil),
      )
    val day3 =
      CurfewTimes(
        fromDay = DayOfWeek.WEDNESDAY,
        fromTime = LocalTime.parse(curfewHours.wednesdayFrom),
        untilDay = DayOfWeek.THURSDAY,
        untilTime = LocalTime.parse(curfewHours.thursdayUntil),
      )
    val day4 =
      CurfewTimes(
        fromDay = DayOfWeek.THURSDAY,
        fromTime = LocalTime.parse(curfewHours.thursdayFrom),
        untilDay = DayOfWeek.FRIDAY,
        untilTime = LocalTime.parse(curfewHours.fridayUntil),
      )
    val day5 =
      CurfewTimes(
        fromDay = DayOfWeek.FRIDAY,
        fromTime = LocalTime.parse(curfewHours.fridayFrom),
        untilDay = DayOfWeek.SATURDAY,
        untilTime = LocalTime.parse(curfewHours.saturdayUntil),
      )
    val day6 =
      CurfewTimes(
        fromDay = DayOfWeek.SATURDAY,
        fromTime = LocalTime.parse(curfewHours.saturdayFrom),
        untilDay = DayOfWeek.SUNDAY,
        untilTime = LocalTime.parse(curfewHours.sundayUntil),
      )
    val day7 =
      CurfewTimes(
        fromDay = DayOfWeek.SUNDAY,
        fromTime = LocalTime.parse(curfewHours.sundayFrom),
        untilDay = DayOfWeek.MONDAY,
        untilTime = LocalTime.parse(curfewHours.mondayUntil),
      )
    return listOf(
      day1,
      day2,
      day3,
      day4,
      day5,
      day6,
      day7,
    )
  }
}
