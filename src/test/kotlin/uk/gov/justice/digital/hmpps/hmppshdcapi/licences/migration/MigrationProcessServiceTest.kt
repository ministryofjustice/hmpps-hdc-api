package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class MigrationProcessServiceTest {

  private lateinit var service: MigrationProcessService
  private val migrationRepository = mock<MigrationRepository>()
  private val migrationRequestService = mock<MigrationRequestService>()
  private val prisonSearchApiClient = mock<PrisonSearchApiClient>()

  private val clock = Clock.fixed(
    LocalDate.of(2026, 8, 5)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant(),
    ZoneId.systemDefault(),
  )

  @Test
  fun `should return immediately when migration is not allowed`() {
    // Given
    val clock = Clock.fixed(
      LocalDate.of(2026, 6, 24)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant(),
      ZoneId.systemDefault(),
    )

    service = createService(
      allowedMigrationDate = LocalDate.of(2026, 6, 25),
      clock = clock,
    )

    // When
    service.migrateABatchOfLicences()

    // Then
    verify(migrationRepository, never()).getMigratableLicenceBatch(any(), any())
  }

  @Test
  fun `should return immediately when allowedMigrationDate IS NOT SET`() {
    // Given
    service = createService()

    // When
    service.migrateABatchOfLicences()

    // Then
    verify(migrationRepository, never()).getMigratableLicenceBatch(any(), any())
  }

  private fun createService(
    allowedMigrationDate: LocalDate? = null,
    clock: Clock = this.clock,
  ) = MigrationProcessService(
    migrationRepository = migrationRepository,
    migrationRequestService = migrationRequestService,
    prisonSearchApiClient = prisonSearchApiClient,
    allowedNroMigrationDate = allowedMigrationDate,
    clock = clock,
  )
}
