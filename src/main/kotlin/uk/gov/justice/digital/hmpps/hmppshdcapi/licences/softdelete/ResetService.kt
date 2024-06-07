package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ResetService(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val auditEventRepository: AuditEventRepository,
  private val prisonApiClient: PrisonApiClient
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun resetLicences(bookingIds: List<Long>): ResetResponse {
    val licences = licenceRepository.findByBookingIds(bookingIds)

    licences.forEach {
      val today = LocalDateTime.now()
      it.deletedAt = today
      auditEventRepository.save(AuditEvent(user = "SYSTEM:API", action = "RESET", timestamp = LocalDateTime.now(), details = mapOf("bookingId" to it.bookingId)))
      softDeleteLicenceVersions(it.bookingId, today)
      val result = prisonApiClient.resetHdcChecks(it.bookingId)
      when {
        result == null -> log.error("Failed to migrate due to unexpected error")
        result.is2xxSuccessful -> log.info("Successfully reset licence with id: ${it.id}")
        result == HttpStatus.CONFLICT -> log.error("Failed to reset licence with id: ${it.id} as currently in use")
        result.isError -> log.info("Failed to reset licence with id: ${it.id} due to status: $result")
        else -> log.error("Unexpected error trying to reset licence, status: $result")
      }
    }
    licenceRepository.saveAllAndFlush(licences)
    return ResetResponse(numberOfResetRecords = licences.size, numberOfResetsRequested = bookingIds.size)
  }

  private fun softDeleteLicenceVersions(bookingId: Long, today: LocalDateTime) {
    val hdcLicenceVersions = licenceVersionRepository.findAllByBookingIdAndDeletedAtIsNull(bookingId)
    for (licenceVersion in hdcLicenceVersions) {
      licenceVersion.deletedAt = today
    }
  }

  data class ResetResponse(
    @Schema(description = "Number of licences reset")
    val numberOfResetRecords: Int,
    @Schema(description = "Number of resets requested")
    val numberOfResetsRequested: Int,
    )
}
