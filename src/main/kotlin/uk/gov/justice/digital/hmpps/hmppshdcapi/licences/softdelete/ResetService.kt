package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.AuditEventType
import java.time.LocalDateTime

@Service
class ResetService(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val auditEventRepository: AuditEventRepository,
  private val prisonApiClient: PrisonApiClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun resetLicences(bookingIds: List<Long>): ResetResponse {
    val licences = licenceRepository.findByBookingIds(bookingIds)
    var licencesResetInNomis = 0
    licences.forEach {
      val today = LocalDateTime.now()
      it.deletedAt = today
      auditEventRepository.save(AuditEvent(user = "${AuditEventType.SYSTEM_API.eventType}", action = "RESET", timestamp = LocalDateTime.now(), details = mapOf("bookingId" to it.bookingId)))
      softDeleteLicenceVersions(it.bookingId, today)
      when (prisonApiClient.resetHdcChecks(it.bookingId)) {
        true -> {
          licencesResetInNomis += 1
          log.info("Successfully reset licence with id: ${it.id}")
        }
        false -> log.error("Failed to reset licence with id: ${it.id}")
      }
    }
    licenceRepository.saveAllAndFlush(licences)

    return ResetResponse(numberOfResetRecordsInHdc = licences.size, numberOfResetsRequested = bookingIds.size, numberOfResetRecordsInNomis = licencesResetInNomis)
      .also { log.info("$it") }
  }

  private fun softDeleteLicenceVersions(bookingId: Long, today: LocalDateTime) {
    val hdcLicenceVersions = licenceVersionRepository.findAllByBookingIdAndDeletedAtIsNull(bookingId)
    for (licenceVersion in hdcLicenceVersions) {
      licenceVersion.deletedAt = today
    }
  }

  data class ResetResponse(
    @Schema(description = "Number of licences reset in hdc")
    val numberOfResetRecordsInHdc: Int,
    @Schema(description = "Number of licences reset in NOMIS")
    val numberOfResetRecordsInNomis: Int,
    @Schema(description = "Number of resets requested")
    val numberOfResetsRequested: Int,
  )
}
