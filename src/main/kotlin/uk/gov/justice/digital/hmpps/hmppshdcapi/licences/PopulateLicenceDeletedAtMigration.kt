package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import java.time.LocalDate
import java.time.LocalDateTime

val UNKNOWN_DELETED_AT = null

@Service
class PopulateLicenceDeletedAtMigration(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val prisonApiClient: PrisonApiClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun run(previousLastIdProcessed: Long, numberToMigrate: Int = 1000) {
    val licencesRecords = licencesToMigrate(previousLastIdProcessed, numberToMigrate)
    do {
      val lastIdProcessed = licencesRecords.content.last().first.id
      log.info("Last Id processed in batch: $lastIdProcessed")
      applyAnySoftDeletes(licencesRecords, numberToMigrate)
    } while (!licencesRecords.isEmpty)
  }

  private fun licencesToMigrate(previousLastIdProcessed: Long, numberToMigrate: Int): Page<Pair<Licence, Booking?>> {
    val hdcLicences = licenceRepository.findAllByDeletedAtAndIdGreaterThanLastProcessedAndOrderByIdAsc(UNKNOWN_DELETED_AT, previousLastIdProcessed, Pageable.ofSize(numberToMigrate))
    val bookings = getBookings(hdcLicences)
    return hdcLicences.map { it to bookings[it.bookingId] }
  }

  private fun getBookings(hdcLicences: Page<Licence>): Map<Long, Booking> {
    val bookings = hdcLicences.content.mapNotNull { prisonApiClient.getBooking(it.bookingId) }
    return bookings.associateBy { it.bookingId }
  }

  fun isToBeSoftDeleted(booking: Booking): Boolean {
    val topupSupervisionExpiryDate = booking.topupSupervisionExpiryDate
    val licenceExpiryDate = booking.licenceExpiryDate
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

  private fun applyAnySoftDeletes(licencesRecords: Page<Pair<Licence, Booking?>>, numberToMigrate: Int): Response {
    val licences = licencesRecords.content.map { (licence, booking) ->
      val today = LocalDateTime.now()

      if (booking != null && isToBeSoftDeleted(booking)) {
        licence.deletedAt = today
        softDeleteLicenceVersions(licence.bookingId, today)
      }
      licence
    }
    licenceRepository.saveAllAndFlush(licences)

    val missingCount = licencesRecords.count { (_, prisoner) -> prisoner == null }

    return Response(
      migrateFail = missingCount,
      migrateSuccess = licencesRecords.content.size - missingCount,
      batchSize = numberToMigrate,
      totalBatches = licencesRecords.totalPages,
      totalRemaining = licencesRecords.totalElements - licences.size,
    )
  }

  data class Response(
    val migrateSuccess: Int,
    val migrateFail: Int,
    val batchSize: Int,
    val totalBatches: Int,
    val totalRemaining: Long,
  )
}
