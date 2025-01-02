package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete

import jakarta.persistence.EntityManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository.LicenceIdentifiers
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.AuditEventType
import java.lang.Thread.sleep
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

@Service
class SoftDeleteService(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val auditEventRepository: AuditEventRepository,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val transactionTemplate: TransactionTemplate,
  private val entityManager: EntityManager,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Async
  fun runJob(batchSize: Int = 500): CompletableFuture<JobResponse> {
    var lastIdProcessed: Long? = 0L
    var totalBatches = 0
    var totalFailedToProcess = 0
    var totalProcessed = 0
    var totalDeleted = 0

    log.info("Starting soft delete job")

    while (lastIdProcessed != null) {
      runInTransaction {
        totalBatches++
        log.info("Running batch: {}", totalBatches)
        val licencesRecords = licencesToMigrate(lastIdProcessed!!, batchSize)
        lastIdProcessed = licencesRecords.content.lastOrNull()?.first?.id

        log.info("Last Id processed in batch: {}", lastIdProcessed ?: " no records processed")
        val deletedLicences = applyAnySoftDeletes(licencesRecords, AuditEventType.SYSTEM_JOB)

        totalProcessed += licencesRecords.numberOfElements
        totalDeleted += deletedLicences.size
        totalFailedToProcess += licencesRecords.count { (_, prisoner) -> prisoner == null }
      }
      sleep(1.seconds.inWholeMilliseconds)
    }

    val response = JobResponse(
      totalProcessed = totalProcessed,
      totalDeleted = totalDeleted,
      totalFailedToProcess = totalFailedToProcess,
      batchSize = batchSize,
      totalBatches = totalBatches,
    )
    log.info("Job finished: {}", response)
    return CompletableFuture.completedFuture(response)
  }

  @Transactional
  fun runMigration(initialIdToProcess: Long, batchSize: Int = 50): MigrationBatchResponse {
    val licencesRecords = licencesToMigrate(initialIdToProcess, batchSize)
    val lastIdProcessed = licencesRecords.content.lastOrNull()?.first?.id
    log.info("Last Id processed in batch: {}", lastIdProcessed ?: " no records processed")
    val licencesToSoftDelete = applyAnySoftDeletes(licencesRecords, AuditEventType.SYSTEM_MIGRATION)
    val totalFailedToProcess = licencesRecords.count { (_, prisoner) -> prisoner == null }

    return MigrationBatchResponse(
      totalProcessed = licencesRecords.content.size,
      totalDeleted = licencesToSoftDelete.size,
      totalFailedToProcess = totalFailedToProcess,
      batchSize = batchSize,
      lastIdProcessed = lastIdProcessed,
    )
  }

  private fun licencesToMigrate(lastIdProcessed: Long, batchSize: Int): Page<Pair<LicenceIdentifiers, Prisoner?>> {
    val hdcLicences = licenceRepository.findAllByIdGreaterThanLastProcessed(lastIdProcessed, Pageable.ofSize(batchSize))
    val prisoners = getPrisoners(hdcLicences)
    return hdcLicences.map { it to prisoners[it.bookingId.toString()] }
  }

  private fun getPrisoners(hdcLicences: Page<LicenceIdentifiers>): Map<String, Prisoner> {
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

  fun applyAnySoftDeletes(
    licencesRecords: Page<Pair<LicenceIdentifiers, Prisoner?>>,
    auditEventType: AuditEventType,
  ): List<LicenceIdentifiers> {
    val licencesToSoftDelete = licencesRecords.content
      .filter { (_, prisoner) -> prisoner != null && isToBeSoftDeleted(prisoner) }
      .map { (licence, _) -> licence }

    log.info("found {} out of {} licences to delete", licencesToSoftDelete.size, licencesRecords.numberOfElements)
    val today = LocalDateTime.now()
    licenceRepository.softDeleteLicence(today, licencesToSoftDelete.map { it.id })
    licenceVersionRepository.softDeleteLicenceVersions(today, licencesToSoftDelete.map { it.bookingId })

    licencesToSoftDelete.forEach {
      auditEventRepository.save(
        AuditEvent(
          user = auditEventType.eventType,
          action = "RESET",
          timestamp = LocalDateTime.now(),
          details = mapOf("bookingId" to it.bookingId),
        ),
      )
    }

    return licencesToSoftDelete
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

  private fun <R> runInTransaction(block: () -> R) =
    transactionTemplate.execute {
      block()
      entityManager.flush()
      entityManager.clear()
    }
}
