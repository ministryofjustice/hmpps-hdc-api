package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence as ModelHdcLicence

/*
** Functions which transform JPA entity objects into their API model equivalents.
** Mostly pass-thru but some translations, so useful to keep the database objects separate from API objects.
*/

fun transformToHdcLicence(
  curfewAddress: String?,
  firstNightCurfewHours: FirstNight?,
  curfewHours: CurfewHours?,
) = ModelHdcLicence(
  curfewAddress = curfewAddress,
  firstNightCurfewHours = firstNightCurfewHours,
  curfewHours = curfewHours,
)
