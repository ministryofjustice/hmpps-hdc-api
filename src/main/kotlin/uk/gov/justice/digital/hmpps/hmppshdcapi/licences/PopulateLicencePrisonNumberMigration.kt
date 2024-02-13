package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient

const val UNKNOWN_PRISON_NUMBER = "???"
const val UNKNOWN_PRISON_NUMBER_BY_PRISON_API = "!!!"

@Service
class PopulateLicencePrisonNumberMigration(
  private val licenceRepository: LicenceRepository,
  private val prisonApiClient: PrisonApiClient,
) {

  @Transactional
  fun run(numberToMigrate: Int): Response {
    val licencesToMigrate = licencesToMigrate(numberToMigrate)

    val licences = licencesToMigrate.content.map { (licence, booking) ->
      val prisonNumber = booking?.offenderNo ?: UNKNOWN_PRISON_NUMBER_BY_PRISON_API
      licence.prisonNumber = prisonNumber
      licence
    }

    licenceRepository.saveAllAndFlush(licences)

    val missingCount = licencesToMigrate.count { (_, prisoner) -> prisoner == null }

    return Response(
      migrateFail = missingCount,
      migrateSuccess = licencesToMigrate.content.size - missingCount,
      batchSize = numberToMigrate,
      totalBatches = licencesToMigrate.totalPages,
      totalRemaining = licencesToMigrate.totalElements - licences.size,
    )
  }

  private fun licencesToMigrate(numberToMigrate: Int): Page<Pair<Licence, Booking?>> {
    val hdcLicences = licenceRepository.findByPrisonNumber(UNKNOWN_PRISON_NUMBER, Pageable.ofSize(numberToMigrate))
    val bookings = getBookings(hdcLicences)
    return hdcLicences.map { it to bookings[it.bookingId] }
  }

  private fun getBookings(hdcLicences: Page<Licence>): Map<Long, Booking> {
    val bookings = hdcLicences.content.mapNotNull { prisonApiClient.getBooking(it.bookingId) }
    return bookings.associateBy { it.bookingId }
  }

  data class Response(
    val migrateSuccess: Int,
    val migrateFail: Int,
    val batchSize: Int,
    val totalBatches: Int,
    val totalRemaining: Long,
  )
}
