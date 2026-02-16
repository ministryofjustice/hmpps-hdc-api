package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.ConvertedLicenseBatch
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.conditions.LicenceIdBatchRequest

@RestController
@RequestMapping("/licences/conditions")
@Tag(name = "Licence Conditions", description = "Operations related to licence bespoke conditions")
class ConditionsController(
  private val conditionsService: ConditionsService,
) {

  @PostMapping("/batch")
  @PreAuthorize("hasAnyRole('$ROLE_HDC_ADMIN')")
  @Operation(
    summary = "Retrieve bespoke conditions for a batch of licences",
    description = "Returns converted bespoke conditions for the supplied licence IDs",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Conditions retrieved successfully",
        content = [
          Content(schema = Schema(implementation = ConvertedLicenseBatch::class)),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request",
      ),
    ],
  )
  fun getBespokeConditions(
    @Valid @RequestBody request: LicenceIdBatchRequest,
  ): ResponseEntity<ConvertedLicenseBatch> {
    val result = conditionsService.getBespokeConditions(request.licenceIds)
    return ResponseEntity.ok(result)
  }
}
