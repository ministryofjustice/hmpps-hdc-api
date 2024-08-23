package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonContactDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Telephone
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
    licence = mapOf(
      "bassReferral" to mapOf(
        "bassRequest" to mapOf(
          "bassRequested" to "No",
        ),
      ),
      "proposedAddress" to mapOf(
        "curfewAddress" to mapOf(
          "addressLine1" to "1 The Street",
          "addressLine2" to "Area",
          "addressTown" to "Town",
          "postCode" to "AB1 2CD",
        ),
      ),
      "curfew" to mapOf(
        "firstNight" to mapOf(
          "firstNightFrom" to "16:00",
          "firstNightUntil" to "08:00",
        ),
        "curfewHours" to mapOf(
          "fridayFrom" to "20:00",
          "mondayFrom" to "20:00",
          "sundayFrom" to "20:00",
          "fridayUntil" to "08:00",
          "mondayUntil" to "08:00",
          "sundayUntil" to "08:00",
          "tuesdayFrom" to "20:00",
          "saturdayFrom" to "20:00",
          "thursdayFrom" to "20:00",
          "tuesdayUntil" to "08:00",
          "saturdayUntil" to "08:00",
          "thursdayUntil" to "08:00",
          "wednesdayFrom" to "20:00",
          "wednesdayUntil" to "08:00",
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
    licence = mapOf(
      "bassReferral" to mapOf(
        "bassOffer" to mapOf(
          "addressLine1" to "2 The Street",
          "addressLine2" to "Area 2",
          "addressTown" to "Town 2",
          "postCode" to "EF3 4GH",
          "bassAccepted" to "Yes",
        ),
        "bassRequest" to mapOf(
          "bassRequested" to "Yes",
        ),
      ),
      "proposedAddress" to emptyMap(),
      "curfew" to mapOf(
        "firstNight" to mapOf(
          "firstNightFrom" to "15:00",
          "firstNightUntil" to "07:00",
        ),
        "curfewHours" to mapOf(
          "fridayFrom" to "19:00",
          "mondayFrom" to "19:00",
          "sundayFrom" to "19:00",
          "fridayUntil" to "07:00",
          "mondayUntil" to "07:00",
          "sundayUntil" to "07:00",
          "tuesdayFrom" to "19:00",
          "saturdayFrom" to "19:00",
          "thursdayFrom" to "19:00",
          "tuesdayUntil" to "07:00",
          "saturdayUntil" to "07:00",
          "thursdayUntil" to "07:00",
          "wednesdayFrom" to "19:00",
          "wednesdayUntil" to "07:00",
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
    licence = mapOf(
      "bassReferral" to mapOf(
        "bassOffer" to mapOf(
          "addressLine1" to "2 The Street",
          "addressLine2" to null,
          "addressTown" to "Town 2",
          "postCode" to "EF3 4GH",
          "bassAccepted" to "Yes",
        ),
        "bassRequest" to mapOf(
          "bassRequested" to "Yes",
        ),
      ),
      "proposedAddress" to emptyMap(),
      "curfew" to mapOf(
        "firstNight" to mapOf(
          "firstNightFrom" to "15:00",
          "firstNightUntil" to "07:00",
        ),
        "curfewHours" to mapOf(
          "fridayFrom" to "19:00",
          "mondayFrom" to "19:00",
          "sundayFrom" to "19:00",
          "fridayUntil" to "07:00",
          "mondayUntil" to "07:00",
          "sundayUntil" to "07:00",
          "tuesdayFrom" to "19:00",
          "saturdayFrom" to "19:00",
          "thursdayFrom" to "19:00",
          "tuesdayUntil" to "07:00",
          "saturdayUntil" to "07:00",
          "thursdayUntil" to "07:00",
          "wednesdayFrom" to "19:00",
          "wednesdayUntil" to "07:00",
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
    licence = mapOf(
      "bassReferral" to mapOf(
        "bassRequest" to mapOf(
          "bassRequested" to "Yes",
        ),
        "approvedPremisesAddress" to mapOf(
          "addressLine1" to "3 The Avenue",
          "addressLine2" to "Area 3",
          "addressTown" to "Town 3",
          "postCode" to "IJ3 4KL",
        ),
        "bassAreaCheck" to mapOf(
          "approvedPremisesRequiredYesNo" to "Yes",
        ),
      ),
      "proposedAddress" to emptyMap(),
      "curfew" to mapOf(
        "firstNight" to mapOf(
          "firstNightFrom" to "15:00",
          "firstNightUntil" to "07:00",
        ),
        "curfewHours" to mapOf(
          "fridayFrom" to "19:00",
          "mondayFrom" to "19:00",
          "sundayFrom" to "19:00",
          "fridayUntil" to "07:00",
          "mondayUntil" to "07:00",
          "sundayUntil" to "07:00",
          "tuesdayFrom" to "19:00",
          "saturdayFrom" to "19:00",
          "thursdayFrom" to "19:00",
          "tuesdayUntil" to "07:00",
          "saturdayUntil" to "07:00",
          "thursdayUntil" to "07:00",
          "wednesdayFrom" to "19:00",
          "wednesdayUntil" to "07:00",
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
    licence = mapOf(
      "bassReferral" to mapOf(
        "bassRequest" to mapOf(
          "bassRequested" to "No",
        ),
        "bassAreaCheck" to mapOf(
          "approvedPremisesRequiredYesNo" to "No",
        ),
      ),
      "proposedAddress" to emptyMap(),
      "curfew" to mapOf(
        "firstNight" to mapOf(
          "firstNightFrom" to "16:00",
          "firstNightUntil" to "08:00",
        ),
        "curfewHours" to mapOf(
          "fridayFrom" to "20:00",
          "mondayFrom" to "20:00",
          "sundayFrom" to "20:00",
          "fridayUntil" to "08:00",
          "mondayUntil" to "08:00",
          "sundayUntil" to "08:00",
          "tuesdayFrom" to "20:00",
          "saturdayFrom" to "20:00",
          "thursdayFrom" to "20:00",
          "tuesdayUntil" to "08:00",
          "saturdayUntil" to "08:00",
          "thursdayUntil" to "08:00",
          "wednesdayFrom" to "20:00",
          "wednesdayUntil" to "08:00",
        ),
        "approvedPremisesAddress" to mapOf(
          "addressLine1" to "4 The Street",
          "addressLine2" to "Area 4",
          "addressTown" to "Town 4",
          "postCode" to "MN4 5OP",
        ),
        "approvedPremises" to mapOf(
          "required" to "Yes",
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

  fun anotherBooking() = Booking(
    offenderNo = "C56789D",
    bookingId = 98765,
    agencyId = "MDI",
    topupSupervisionExpiryDate = LocalDate.of(2024, 8, 14),
    licenceExpiryDate = LocalDate.of(2024, 8, 14),
  )

  fun somePrisonInformation() = PrisonContactDetails(
    agencyId = "MDI",
    phones = listOf(
      Telephone(
        number = "0123 456 7890",
        type = "BUS",
      ),
      Telephone(
        number = "0800 123 4567",
        type = "FAX",
      ),
    ),
  )
}
