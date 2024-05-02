package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PopulateLicenceDeletedAtMigration(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val prisonSearchApiClient: PrisonSearchApiClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun run(lastIdProcessed: Long, numberToMigrate: Int = 1000): Response {
    val licencesRecords = licencesToMigrate(lastIdProcessed, numberToMigrate)
    do {
      val lastIdProcessed = licencesRecords.content.lastOrNull()?.first?.id
      log.info("Last Id processed in batch: ${lastIdProcessed ?: " no records processed"}")
      val licences = applyAnySoftDeletes(licencesRecords)

      licenceRepository.saveAllAndFlush(licences)

      val missingCount = licencesRecords.count { (_, prisoner) -> prisoner == null }

      return Response(
        migrateFail = missingCount,
        migrateSuccess = licencesRecords.content.size - missingCount,
        batchSize = numberToMigrate,
        totalBatches = licencesRecords.totalPages,
        totalRemaining = licencesRecords.totalElements - licences.size,
        lastIdProcessed = "${lastIdProcessed ?: " no records processed"}",
      )
    } while (!licencesRecords.isEmpty)
  }

  private fun licencesToMigrate(lastIdProcessed: Long, numberToMigrate: Int): Page<Pair<Licence, Prisoner?>> {
    val hdcLicences = licenceRepository.findAllByDeletedAtAndIdGreaterThanLastProcessedOrderByIdAsc(lastIdProcessed, Pageable.ofSize(numberToMigrate))
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
    if (topupSupervisionExpiryDate == null && licenceExpiryDate != null) {
      return licenceExpiryDate <= today
    }
    if (topupSupervisionExpiryDate != null && licenceExpiryDate != null && topupSupervisionExpiryDate < licenceExpiryDate) {
      return licenceExpiryDate <= today
    }
    if (topupSupervisionExpiryDate != null) {
      return topupSupervisionExpiryDate <= today
    }
    return false
  }

  private fun softDeleteLicenceVersions(bookingId: Long, today: LocalDateTime) {
    val hdcLicenceVersions = licenceVersionRepository.findAllByBookingId(bookingId)
    for (licenceVersion in hdcLicenceVersions) {
      licenceVersion.deletedAt = today
    }
    licenceVersionRepository.saveAllAndFlush(hdcLicenceVersions)
  }

  private fun applyAnySoftDeletes(licencesRecords: Page<Pair<Licence, Prisoner?>>): List<Licence> {
    val licences = licencesRecords.content.map { (licence, prisoner) ->
      val today = LocalDateTime.now()

      if (prisoner != null && isToBeSoftDeleted(prisoner)) {
        licence.deletedAt = today
        softDeleteLicenceVersions(licence.bookingId, today)
      }
      licence
    }
    return licences
  }

  data class Response(
    val migrateSuccess: Int,
    val migrateFail: Int,
    val batchSize: Int,
    val totalBatches: Int,
    val totalRemaining: Long,
    val lastIdProcessed: String,
  )
}
