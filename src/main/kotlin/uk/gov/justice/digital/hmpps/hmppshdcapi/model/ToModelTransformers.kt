package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Address
import java.time.DayOfWeek
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours as EntityCurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight as EntityFirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewAddress as ModelCurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewTimes as ModelCurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.FirstNight as ModelFirstNight

fun transformToModelCurfewAddress(
  address: Pair<Address?, AddressType>,
): ModelCurfewAddress = ModelCurfewAddress(
  addressLine1 = address.first?.addressLine1,
  addressLine2 = address.first?.addressLine2,
  townOrCity = address.first?.addressTown,
  county = null,
  postcode = address.first?.postCode,
  curfewAddressType = address.second,
)

fun transformToModelCurfewTimes(
  curfewHours: EntityCurfewTimes?,
): List<ModelCurfewTimes> = listOf(
  ModelCurfewTimes(
    fromDay = DayOfWeek.MONDAY,
    fromTime = curfewHours?.mondayFrom,
    untilDay = DayOfWeek.TUESDAY,
    untilTime = curfewHours?.tuesdayUntil,
  ),
  ModelCurfewTimes(
    fromDay = DayOfWeek.TUESDAY,
    fromTime = curfewHours?.tuesdayFrom,
    untilDay = DayOfWeek.WEDNESDAY,
    untilTime = curfewHours?.wednesdayUntil,
  ),
  ModelCurfewTimes(
    fromDay = DayOfWeek.WEDNESDAY,
    fromTime = curfewHours?.wednesdayFrom,
    untilDay = DayOfWeek.THURSDAY,
    untilTime = curfewHours?.thursdayUntil,
  ),
  ModelCurfewTimes(
    fromDay = DayOfWeek.THURSDAY,
    fromTime = curfewHours?.thursdayFrom,
    untilDay = DayOfWeek.FRIDAY,
    untilTime = curfewHours?.fridayUntil,
  ),
  ModelCurfewTimes(
    fromDay = DayOfWeek.FRIDAY,
    fromTime = curfewHours?.fridayFrom,
    untilDay = DayOfWeek.SATURDAY,
    untilTime = curfewHours?.saturdayUntil,
  ),
  ModelCurfewTimes(
    fromDay = DayOfWeek.SATURDAY,
    fromTime = curfewHours?.saturdayFrom,
    untilDay = DayOfWeek.SUNDAY,
    untilTime = curfewHours?.sundayUntil,
  ),
  ModelCurfewTimes(
    fromDay = DayOfWeek.SUNDAY,
    fromTime = curfewHours?.sundayFrom,
    untilDay = DayOfWeek.MONDAY,
    untilTime = curfewHours?.mondayUntil,
  ),
)

fun transformToModelFirstNight(
  firstNight: EntityFirstNight,
): ModelFirstNight = ModelFirstNight(
  firstNightFrom = firstNight.firstNightFrom,
  firstNightUntil = firstNight.firstNightUntil,
)
