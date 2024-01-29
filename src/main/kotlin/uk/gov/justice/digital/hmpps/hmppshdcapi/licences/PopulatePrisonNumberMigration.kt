package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner

const val UNKNOWN_PRISON_NUMBER = "???"

@Service
class PopulatePrisonNumberMigration(
  private val licenceRepository: LicenceRepository,
  private val prisonSearchApiClient: PrisonSearchApiClient,
) {

  @Transactional
  fun run(numberToMigrate: Int): Response {
    val licencesToMigrate = licencesToMigrate(numberToMigrate)

    val licences = licencesToMigrate.content.map { (licence, prisoner) ->
      val prisonNumber = prisoner?.prisonerNumber ?: UNKNOWN_PRISON_NUMBER
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

  private fun licencesToMigrate(numberToMigrate: Int): Page<Pair<Licence, Prisoner?>> {
    val hdcLicences = licenceRepository.findByPrisonNumberIsNull(Pageable.ofSize(numberToMigrate))
    val prisoners = getPrisoners(hdcLicences)
    return hdcLicences.map { it to prisoners[it.bookingId.toString()] }
  }

  private fun getPrisoners(hdcLicences: Page<Licence>): Map<String, Prisoner> {
    val bookingIds = hdcLicences.content.map { it.bookingId }
    val prisoners = prisonSearchApiClient.getPrisonersByBookingIds(bookingIds)
    return prisoners.associateBy { it.bookingId }
  }

  data class Response(
    val migrateSuccess: Int,
    val migrateFail: Int,
    val batchSize: Int,
    val totalBatches: Int,
    val totalRemaining: Long,
  )
}
