package uk.gov.justice.digital.hmpps.hmppshdcapi.model

import java.time.DayOfWeek
import java.time.LocalTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress as EntityCurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours as EntityCurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight as EntityFirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewAddress as ModelCurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewTimes as ModelCurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.FirstNight as ModelFirstNight

fun transformToModelCurfewAddress(
  address: EntityCurfewAddress,
): ModelCurfewAddress = ModelCurfewAddress(
  addressLine1 = address.addressLine1,
  addressLine2 = address.addressLine2,
  addressTown = address.addressTown,
  postCode = address.postCode,
)

fun transformToModelCurfewTimes(
  curfewHours: EntityCurfewTimes?,
): List<ModelCurfewTimes> {
  return listOf(
    ModelCurfewTimes(
      fromDay = DayOfWeek.MONDAY,
      fromTime = curfewHours?.mondayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.TUESDAY,
      untilTime = curfewHours?.tuesdayUntil?.let { LocalTime.parse(it) },
    ),
    ModelCurfewTimes(
      fromDay = DayOfWeek.TUESDAY,
      fromTime = curfewHours?.tuesdayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.WEDNESDAY,
      untilTime = curfewHours?.wednesdayUntil?.let { LocalTime.parse(it) },
    ),
    ModelCurfewTimes(
      fromDay = DayOfWeek.WEDNESDAY,
      fromTime = curfewHours?.wednesdayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.THURSDAY,
      untilTime = curfewHours?.thursdayUntil?.let { LocalTime.parse(it) },
    ),
    ModelCurfewTimes(
      fromDay = DayOfWeek.THURSDAY,
      fromTime = curfewHours?.thursdayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.FRIDAY,
      untilTime = curfewHours?.fridayUntil?.let { LocalTime.parse(it) },
    ),
    ModelCurfewTimes(
      fromDay = DayOfWeek.FRIDAY,
      fromTime = curfewHours?.fridayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.SATURDAY,
      untilTime = curfewHours?.saturdayUntil?.let { LocalTime.parse(it) },
    ),
    ModelCurfewTimes(
      fromDay = DayOfWeek.SATURDAY,
      fromTime = curfewHours?.saturdayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.SUNDAY,
      untilTime = curfewHours?.sundayUntil?.let { LocalTime.parse(it) },
    ),
    ModelCurfewTimes(
      fromDay = DayOfWeek.SUNDAY,
      fromTime = curfewHours?.sundayFrom?.let { LocalTime.parse(it) },
      untilDay = DayOfWeek.MONDAY,
      untilTime = curfewHours?.mondayUntil?.let { LocalTime.parse(it) },
    ),
  )
}

fun transformToModelFirstNight(
  firstNight: EntityFirstNight?,
): ModelFirstNight = ModelFirstNight(
  firstNightFrom = firstNight?.firstNightFrom,
  firstNightUntil = firstNight?.firstNightUntil,
)
