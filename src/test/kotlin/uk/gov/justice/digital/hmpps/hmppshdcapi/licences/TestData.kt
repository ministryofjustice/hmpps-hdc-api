package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import java.time.LocalDateTime

object TestData {
  fun preferredAddresslicence() = Licence(
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
      "bookingId" to 54321,
      "licence" to mapOf(
        "bassReferral" to mapOf(
          "bassRequest" to mapOf(
            "bassRequested" to "No",
          ),
        ),
        "proposedAddress" to mapOf(
          "curfewAddress" to mapOf(
            "addressLine1" to "1 The Street",
            "addressLine2" to "Area",
            "town" to "Town",
            "postcode" to "AB1 2CD",
          ),
        ),
      ),
    ),
  )

  fun cas2licence() = Licence(
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
      "bookingId" to 54321,
      "licence" to mapOf(
        "bassReferral" to mapOf(
          "bassOffer" to mapOf(
            "addressLine1" to "1 The Street",
            "addressLine2" to "Area",
            "town" to "Town",
            "postcode" to "AB1 2CD",
          ),
          "bassRequest" to mapOf(
            "bassRequested" to "Yes",
          ),
        ),
        "proposedAddress" to emptyMap(),
      ),
    ),
  )
}
