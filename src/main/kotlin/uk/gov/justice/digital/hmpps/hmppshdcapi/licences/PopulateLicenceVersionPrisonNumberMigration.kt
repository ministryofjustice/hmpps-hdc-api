package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient

@Service
class PopulateLicenceVersionPrisonNumberMigration(
  private val licenceVersionRepository: LicenceVersionRepository,
  private val prisonApiClient: PrisonApiClient,
) {

  @Transactional
  fun run(numberToMigrate: Int): Response {
    val versionsToMigrate = versionsToMigrate(numberToMigrate)

    val versions = versionsToMigrate.content.map { (licence, prisoner) ->
      val prisonNumber = prisoner?.offenderNo ?: UNKNOWN_PRISON_NUMBER_BY_PRISON_API
      licence.prisonNumber = prisonNumber
      licence
    }

    licenceVersionRepository.saveAllAndFlush(versions)

    val missingCount = versionsToMigrate.count { (_, prisoner) -> prisoner == null }

    return Response(
      migrateFail = missingCount,
      migrateSuccess = versionsToMigrate.content.size - missingCount,
      batchSize = numberToMigrate,
      totalBatches = versionsToMigrate.totalPages,
      totalRemaining = versionsToMigrate.totalElements - versions.size,
    )
  }

  private fun versionsToMigrate(numberToMigrate: Int): Page<Pair<LicenceVersion, Booking?>> {
    val hdcLicences = licenceVersionRepository.findByPrisonNumber(UNKNOWN_PRISON_NUMBER, Pageable.ofSize(numberToMigrate))
    val prisoners = getBookings(hdcLicences)
    return hdcLicences.map { it to prisoners[it.bookingId] }
  }

  private fun getBookings(versions: Page<LicenceVersion>): Map<Long, Booking> {
    val bookings = versions.content.mapNotNull { prisonApiClient.getBooking(it.bookingId) }
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
