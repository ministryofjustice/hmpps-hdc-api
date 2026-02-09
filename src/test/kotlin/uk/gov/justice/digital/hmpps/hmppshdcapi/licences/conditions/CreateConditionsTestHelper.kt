package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStage
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData

fun createConditions(additionalData: Map<String, Map<String, String>>): LicenceConditions = LicenceConditions(
  bespoke = null,
  standard = null,
  additional = additionalData,
  conditionsSummary = null,
)

fun createLicenceData(
  licenceConditions: LicenceConditions? = null,
) = LicenceData(
  eligibility = null,
  bassReferral = null,
  proposedAddress = null,
  curfew = null,
  risk = null,
  reporting = null,
  victim = null,
  licenceConditions = licenceConditions,
  document = null,
  approval = null,
  finalChecks = null,
)

fun createLicence(
  licenceData: LicenceData? = null,
  additionalConditionsVersion: Int? = 1,
) = Licence(
  id = 1,
  prisonNumber = "A1234BC",
  bookingId = 123,
  stage = HdcStage.ELIGIBILITY,
  version = 1,
  transitionDate = null,
  varyVersion = 1,
  additionalConditionsVersion = additionalConditionsVersion,
  standardConditionsVersion = 1,
  deletedAt = null,
  licenceInCvl = false,
  licence = licenceData,
)
