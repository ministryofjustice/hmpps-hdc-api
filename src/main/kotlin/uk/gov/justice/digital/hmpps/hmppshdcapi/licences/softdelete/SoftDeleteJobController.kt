package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService.JobResponse

@RestController
@RequestMapping("/jobs", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoftDeleteJobController(
  private val softDeleteService: SoftDeleteService,
) {
  @Operation(
    summary = "Job to delete licences that are no longer active.",
    description = "Job to delete licences that are no longer active.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Migration response",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = JobResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  @PostMapping("/delete-inactive-licences/{batchSize}")
  @ResponseBody
  fun populateDeletedAtForLicences(
    @PathVariable(name = "batchSize")
    @Parameter(name = "batchSize", description = "This is the number of licences to process in one batch")
    @Min(1)
    @Max(1000)
    batchSize: Int,
  ) = softDeleteService.runJob(batchSize)
}
