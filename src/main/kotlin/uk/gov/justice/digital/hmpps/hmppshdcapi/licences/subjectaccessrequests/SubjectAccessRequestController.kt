package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.subjectaccessrequests

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse

const val SAR_ROLE = "SAR_DATA_ACCESS"
const val HDC_SAR_ROLE = "HDC_ADMIN"

private const val HTTP_STATUS_NOT_SUPPORTED = 209

private val NOT_SUPPORTED = ResponseEntity
  .status(HTTP_STATUS_NOT_SUPPORTED).body<Any>(
    ErrorResponse(
      status = HTTP_STATUS_NOT_SUPPORTED,
      userMessage = "Search by crn is not supported.",
      developerMessage = "Search by crn is not supported.",
    ),
  )

private val NO_CONTENT = ResponseEntity
  .status(HttpStatus.NO_CONTENT).body<Any>(
    ErrorResponse(
      status = HttpStatus.NO_CONTENT,
      userMessage = "No records found for the prn.",
      developerMessage = "No records found for the prn.",
    ),
  )

@RestController
@PreAuthorize("hasAnyRole('$SAR_ROLE', '$HDC_SAR_ROLE')")
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class SubjectAccessRequestController(private val subjectAccessRequestService: SubjectAccessRequestService) {
  @GetMapping(value = ["/subject-access-request"])
  @ResponseBody
  @Operation(
    summary = "Get a list of licences and audits summaries matching the nomis Prison Reference Number(prn).",
    description = "Returns a list of licences and audit details for the Prison Reference Number (prn). " +
      "Requires ROLE_$SAR_ROLE or ROLE_$HDC_SAR_ROLE.",
    security = [SecurityRequirement(name = "ROLE_$SAR_ROLE"), SecurityRequirement(name = "ROLE_$HDC_SAR_ROLE")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Records found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SarContent::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "204",
        description = "Records for this prn was not found.",
      ),
      ApiResponse(
        responseCode = "$HTTP_STATUS_NOT_SUPPORTED",
        description = "Search by crn is not supported.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
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
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error occurred",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getSarRecordsById(
    @RequestParam(name = "prn", required = false) prn: String?,
    @RequestParam(name = "crn", required = false) crn: String?,
  ): ResponseEntity<Any> {
    check(!(crn != null && prn != null)) { "Only supports search by single identifier." }

    if (crn != null) {
      return NOT_SUPPORTED
    }

    val result = prn?.let { subjectAccessRequestService.getByPrisonNumber(it) }
    return if (result == null) {
      NO_CONTENT
    } else {
      ResponseEntity.status(HttpStatus.OK).body(result)
    }
  }
}
