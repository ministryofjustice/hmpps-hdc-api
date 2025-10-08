package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARLicence
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARLicenceVersion

@Schema(description = "The list of licences, licence versions and audit events")
data class Content(
  @field:Schema(description = "The list of licences")
  val licences: List<SARLicence>,

  @field:Schema(description = "The list of licence versions")
  val licenceVersions: List<SARLicenceVersion>,
)
