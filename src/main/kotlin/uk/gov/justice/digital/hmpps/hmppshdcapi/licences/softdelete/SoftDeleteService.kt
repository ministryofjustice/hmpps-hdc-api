package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SoftDeleteService(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val auditEventRepository: AuditEventRepository,
  private val prisonSearchApiClient: PrisonSearchApiClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun runJob(batchSize: Int = 1000): JobResponse {
    var lastIdProcessed: Long? = 0L
    var totalBatches = 0
    var totalFailedToProcess = 0
    var totalProcessed = 0
    var totalDeleted = 0

    while (lastIdProcessed != null) {
      val licencesRecords = licencesToMigrate(lastIdProcessed, batchSize)
      lastIdProcessed = licencesRecords.content.lastOrNull()?.first?.id

      log.info("Last Id processed in batch: ${lastIdProcessed ?: " no records processed"}")
      val deletedLicences = applyAnySoftDeletes(licencesRecords)

      licenceRepository.saveAllAndFlush(deletedLicences)

      totalBatches++
      totalProcessed += licencesRecords.size
      totalDeleted += deletedLicences.size
      totalFailedToProcess += licencesRecords.count { (_, prisoner) -> prisoner == null }
    }

    return JobResponse(
      totalProcessed = totalProcessed,
      totalDeleted = totalDeleted,
      totalFailedToProcess = totalFailedToProcess,
      batchSize = batchSize,
      totalBatches = totalBatches,
    )
  }

  private fun licencesToMigrate(numberToMigrate: Int): Page<Pair<Licence, Prisoner?>> {
    val hdcLicences = licenceRepository.findAllByDeletedAtOrderByIdAsc(Pageable.ofSize(numberToMigrate))
    val prisoners = getPrisoners(hdcLicences)
    return hdcLicences.map { it to prisoners[it.bookingId.toString()] }
  }

  @Transactional
  fun runMigration(initialIdToProcess: Long, numberToMigrate: Int = 1000): MigrationBatchResponse {
    val licencesRecords = licencesToMigrate(initialIdToProcess, numberToMigrate)
    val lastIdProcessed = licencesRecords.content.lastOrNull()?.first?.id
    log.info("Last Id processed in batch: ${lastIdProcessed ?: " no records processed"}")
    val licencesToSoftDelete = applyAnySoftDeletes(licencesRecords)

    licenceRepository.saveAllAndFlush(licencesToSoftDelete)

    val totalFailedToProcess = licencesRecords.count { (_, prisoner) -> prisoner == null }

    return MigrationBatchResponse(
      totalProcessed = licencesRecords.content.size,
      totalDeleted = licencesToSoftDelete.size,
      totalFailedToProcess = totalFailedToProcess,
      batchSize = numberToMigrate,
      lastIdProcessed = lastIdProcessed,
    )
  }

  private fun licencesToMigrate(lastIdProcessed: Long, numberToMigrate: Int): Page<Pair<Licence, Prisoner?>> {
    val hdcLicences = licenceRepository.findAllByIdGreaterThanLastProcessed(lastIdProcessed, Pageable.ofSize(numberToMigrate))
    val prisoners = getPrisoners(hdcLicences)
    return hdcLicences.map { it to prisoners[it.bookingId.toString()] }
  }

  private fun getPrisoners(hdcLicences: Page<Licence>): Map<String, Prisoner> {
    val bookingIds = hdcLicences.content.map { it.bookingId }
    val prisoners = prisonSearchApiClient.getPrisonersByBookingIds(bookingIds)
    return prisoners.associateBy { it.bookingId }
  }

  fun isToBeSoftDeleted(prisoner: Prisoner): Boolean {
    val topupSupervisionExpiryDate = prisoner.topupSupervisionExpiryDate
    val licenceExpiryDate = prisoner.licenceExpiryDate
    val today = LocalDate.now()

    return when {
      topupSupervisionExpiryDate == null && licenceExpiryDate != null ->
        licenceExpiryDate <= today

      topupSupervisionExpiryDate != null && licenceExpiryDate != null && topupSupervisionExpiryDate < licenceExpiryDate ->
        licenceExpiryDate <= today

      topupSupervisionExpiryDate != null ->
        topupSupervisionExpiryDate <= today

      else -> false
    }
  }

  fun applyAnySoftDeletes(licencesRecords: Page<Pair<Licence, Prisoner?>>): List<Licence> {
    val licencesToSoftDelete = licencesRecords.content
      .filter { (_, prisoner) -> prisoner != null && isToBeSoftDeleted(prisoner) }
      .map { (licence, _) -> licence }

    licencesToSoftDelete.forEach {
      val today = LocalDateTime.now()
      it.deletedAt = today
      auditEventRepository.save(AuditEvent(user = "SYSTEM_EVENT", action = "LICENCE SOFT DELETED", details = mapOf("bookingId" to it.bookingId)))
      softDeleteLicenceVersions(it.bookingId, today)
    }

    return licencesToSoftDelete
  }

  private fun softDeleteLicenceVersions(bookingId: Long, today: LocalDateTime) {
    val hdcLicenceVersions = licenceVersionRepository.findAllByBookingIdAndDeletedAtIsNull(bookingId)
    for (licenceVersion in hdcLicenceVersions) {
      licenceVersion.deletedAt = today
    }
    licenceVersionRepository.saveAllAndFlush(hdcLicenceVersions)
  }

  data class JobResponse(
    /* How many licences seen, deleted or otherwise */
    val totalProcessed: Int,
    /* How many licences deleted */
    val totalDeleted: Int,
    /* How many licences failed to process due to missing prisoner record */
    val totalFailedToProcess: Int,
    val batchSize: Int,
    val totalBatches: Int,
  )

  data class MigrationBatchResponse(
    val totalProcessed: Int,
    val totalDeleted: Int,
    val totalFailedToProcess: Int,
    val batchSize: Int,
    val lastIdProcessed: Long?,
  )
}
