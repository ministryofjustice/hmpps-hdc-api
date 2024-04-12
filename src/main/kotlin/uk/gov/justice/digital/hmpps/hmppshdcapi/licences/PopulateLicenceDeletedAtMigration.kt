package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Limit
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import java.time.LocalDateTime

val UNKNOWN_DELETED_AT = null
const val NUMBER_TO_MIGRATE = 1000

@Service
class PopulateLicenceDeletedAtMigration(
  private val licenceRepository: LicenceRepository,
  private val licenceVersionRepository: LicenceVersionRepository,
  private val prisonApiClient: PrisonApiClient,
) {

  @Transactional
  fun run(previousLastIdProcessed: Long) {
    val licencesRecords = licencesToMigrate(previousLastIdProcessed)
    do {
      val lastIdProcessed = licencesRecords.content.last().first.id
      println(lastIdProcessed)
      applyAnySoftDeletes(licencesRecords)
    } while (!licencesRecords.isEmpty)
  }

  private fun licencesToMigrate(previousLastIdProcessed: Long): Page<Pair<Licence, Booking?>> {
    val hdcLicences = licenceRepository.findAllByDeletedAtAndIdGreaterThanLastProcessedAndOrderByIdAsc(UNKNOWN_DELETED_AT, previousLastIdProcessed, Limit.of(NUMBER_TO_MIGRATE))
    val bookings = getBookings(hdcLicences)
    return hdcLicences.map { it to bookings[it.bookingId] }
  }

  private fun getBookings(hdcLicences: Page<Licence>): Map<Long, Booking> {
    val bookings = hdcLicences.content.mapNotNull { prisonApiClient.getBooking(it.bookingId) }
    return bookings.associateBy { it.bookingId }
  }

  private fun softDeleteLicenceVersions(bookingId: Long) {
    val hdcLicenceVersions = licenceVersionRepository.findAllByBookingId(bookingId)
    val today = LocalDateTime.now()
    for (licenceVersion in hdcLicenceVersions) {
      licenceVersion.deletedAt = today
    }
    licenceVersionRepository.saveAllAndFlush(hdcLicenceVersions)
  }

  private fun applyAnySoftDeletes(licencesRecords: Page<Pair<Licence, Booking?>>): Response {
    val licences = licencesRecords.content.map { (licence, booking) ->
      val topupSupervisionExpiryDate = booking?.topupSupervisionExpiryDate
      val licenceExpiryDate = booking?.licenceExpiryDate
      val today = LocalDateTime.now()

      if (LocalDateTime.parse(topupSupervisionExpiryDate) < LocalDateTime.parse(licenceExpiryDate)) {
        val isLEDTodayOrPast = LocalDateTime.parse(licenceExpiryDate) <= today
        if (isLEDTodayOrPast) {
          licence.deletedAt = today
          softDeleteLicenceVersions(licence.bookingId)
        }
      } else if (topupSupervisionExpiryDate != null) {
        val isTUSEDTodayOrPast = LocalDateTime.parse(topupSupervisionExpiryDate) <= today
        if (isTUSEDTodayOrPast) {
          licence.deletedAt = today
          softDeleteLicenceVersions(licence.bookingId)
        }
      } else {
        val isLEDTodayOrPast = LocalDateTime.parse(licenceExpiryDate) <= today
        if (isLEDTodayOrPast) {
          licence.deletedAt = today
          softDeleteLicenceVersions(licence.bookingId)
        }
      }
      licence
    }
    licenceRepository.saveAllAndFlush(licences)

    val missingCount = licencesRecords.count { (_, prisoner) -> prisoner == null }

    return Response(
      migrateFail = missingCount,
      migrateSuccess = licencesRecords.content.size - missingCount,
      batchSize = NUMBER_TO_MIGRATE,
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
