package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner

@Service
class PopulateLicenceVersionPrisonNumberMigration(
  private val licenceVersionRepository: LicenceVersionRepository,
  private val prisonSearchApiClient: PrisonSearchApiClient,
) {

  @Transactional
  fun run(numberToMigrate: Int): Response {
    val versionsToMigrate = versionsToMigrate(numberToMigrate)

    val versions = versionsToMigrate.content.map { (licence, prisoner) ->
      val prisonNumber = prisoner?.prisonerNumber ?: UNKNOWN_PRISON_NUMBER
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

  private fun versionsToMigrate(numberToMigrate: Int): Page<Pair<LicenceVersion, Prisoner?>> {
    val hdcLicences = licenceVersionRepository.findByPrisonNumberIsNull(Pageable.ofSize(numberToMigrate))
    val prisoners = getPrisoners(hdcLicences)
    return hdcLicences.map { it to prisoners[it.bookingId.toString()] }
  }

  private fun getPrisoners(versions: Page<LicenceVersion>): Map<String, Prisoner> {
    val bookingIds = versions.content.map { it.bookingId }
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
