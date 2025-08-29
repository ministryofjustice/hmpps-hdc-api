package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARLicence
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARLicenceVersion

@Schema(description = "The list of licences, licence versions and audit events")
data class Content(
  @Schema(description = "The list of licences")
  val licences: List<SARLicence>,

  @Schema(description = "The list of licence versions")
  val licenceVersions: List<SARLicenceVersion>,

)

@Schema(description = "The Sar Content holds the prisoner details")
data class SarContent(
  @Schema(description = "SAR content")
  val content: Content,
)
