package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {
  fun aPreferredAddressLicence() = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licence = LicenceData(
      bassReferral = Cas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.NO),
        bassOffer = null,
      ),
      proposedAddress = ProposedAddress(
        CurfewAddress(
          addressLine1 = "1 The Street",
          addressLine2 = "Area",
          addressTown = "Town",
          postCode = "AB1 2CD",
        ),
      ),
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = "16:00", firstNightUntil = "08:00"),
        curfewHours = CurfewHours(
          mondayFrom = "20:00",
          mondayUntil = "08:00",
          tuesdayFrom = "20:00",
          tuesdayUntil = "08:00",
          wednesdayFrom = "20:00",
          wednesdayUntil = "08:00",
          thursdayFrom = "20:00",
          thursdayUntil = "08:00",
          fridayFrom = "20:00",
          fridayUntil = "08:00",
          saturdayFrom = "20:00",
          saturdayUntil = "08:00",
          sundayFrom = "20:00",
          sundayUntil = "08:00",
        ),
      ),
    ),
  )

  fun aCas2Licence() = Licence(
    id = 2,
    prisonNumber = "C56789D",
    bookingId = 98765,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licence = LicenceData(
      bassReferral = Cas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = Cas2Offer(
          addressLine1 = "2 The Street",
          addressLine2 = "Area 2",
          addressTown = "Town 2",
          postCode = "EF3 4GH",
          bassAccepted = OfferAccepted.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = "15:00", firstNightUntil = "07:00"),
        curfewHours = CurfewHours(
          mondayFrom = "19:00",
          mondayUntil = "07:00",
          tuesdayFrom = "19:00",
          tuesdayUntil = "07:00",
          wednesdayFrom = "19:00",
          wednesdayUntil = "07:00",
          thursdayFrom = "19:00",
          thursdayUntil = "07:00",
          fridayFrom = "19:00",
          fridayUntil = "07:00",
          saturdayFrom = "19:00",
          saturdayUntil = "07:00",
          sundayFrom = "19:00",
          sundayUntil = "07:00",
        ),
      ),
    ),
  )

  fun aCas2LicenceWithShortAddress() = Licence(
    id = 3,
    prisonNumber = "C56789D",
    bookingId = 98765,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licence = LicenceData(
      bassReferral = Cas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = Cas2Offer(
          addressLine1 = "2 The Street",
          addressLine2 = null,
          addressTown = "Town 2",
          postCode = "EF3 4GH",
          bassAccepted = OfferAccepted.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = "15:00", firstNightUntil = "07:00"),
        curfewHours = CurfewHours(
          mondayFrom = "19:00",
          mondayUntil = "07:00",
          tuesdayFrom = "19:00",
          tuesdayUntil = "07:00",
          wednesdayFrom = "19:00",
          wednesdayUntil = "07:00",
          thursdayFrom = "19:00",
          thursdayUntil = "07:00",
          fridayFrom = "19:00",
          fridayUntil = "07:00",
          saturdayFrom = "19:00",
          saturdayUntil = "07:00",
          sundayFrom = "19:00",
          sundayUntil = "07:00",
        ),
      ),
    ),
  )

  fun aCas2ApprovedPremisesLicence() = Licence(
    id = 4,
    prisonNumber = "C56789D",
    bookingId = 98765,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licence = LicenceData(
      bassReferral = Cas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = null,
        approvedPremisesAddress = CurfewAddress(
          addressLine1 = "3 The Avenue",
          addressLine2 = "Area 3",
          addressTown = "Town 3",
          postCode = "IJ3 4KL",
        ),
        bassAreaCheck = Cas2AreaCheck(
          approvedPremisesRequiredYesNo = Decision.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = "15:00", firstNightUntil = "07:00"),
        curfewHours = CurfewHours(
          mondayFrom = "19:00",
          mondayUntil = "07:00",
          tuesdayFrom = "19:00",
          tuesdayUntil = "07:00",
          wednesdayFrom = "19:00",
          wednesdayUntil = "07:00",
          thursdayFrom = "19:00",
          thursdayUntil = "07:00",
          fridayFrom = "19:00",
          fridayUntil = "07:00",
          saturdayFrom = "19:00",
          saturdayUntil = "07:00",
          sundayFrom = "19:00",
          sundayUntil = "07:00",
        ),
      ),
    ),
  )

  fun aCurfewApprovedPremisesRequiredLicence() = Licence(
    id = 5,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licence = LicenceData(
      bassReferral = Cas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.NO),
        bassOffer = null,
        approvedPremisesAddress = null,
        bassAreaCheck = Cas2AreaCheck(
          approvedPremisesRequiredYesNo = Decision.NO,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = "16:00", firstNightUntil = "08:00"),
        curfewHours = CurfewHours(
          mondayFrom = "20:00",
          mondayUntil = "08:00",
          tuesdayFrom = "20:00",
          tuesdayUntil = "08:00",
          wednesdayFrom = "20:00",
          wednesdayUntil = "08:00",
          thursdayFrom = "20:00",
          thursdayUntil = "08:00",
          fridayFrom = "20:00",
          fridayUntil = "08:00",
          saturdayFrom = "20:00",
          saturdayUntil = "08:00",
          sundayFrom = "20:00",
          sundayUntil = "08:00",
        ),
        approvedPremisesAddress = CurfewAddress(
          addressLine1 = "4 The Street",
          addressLine2 = "Area 4",
          addressTown = "Town 4",
          postCode = "MN4 5OP",
        ),
        approvedPremises = ApprovedPremises(
          required = Decision.YES,
        ),
      ),
    ),
  )

  fun aBooking() = Booking(
    offenderNo = "A12345B",
    bookingId = 54321,
    agencyId = "MDI",
    topupSupervisionExpiryDate = LocalDate.of(2024, 8, 14),
    licenceExpiryDate = LocalDate.of(2024, 8, 14),
  )
}
