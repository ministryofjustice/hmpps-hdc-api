package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
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

  @Test
  fun `should return true when prisoner is within release event window`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(LocalDate.of(2026, 8, 1))
    whenever(prisoner.conditionalReleaseDate).thenReturn(LocalDate.of(2026, 8, 20))

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `should return false when home detention curfew actual date is null`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(null)
    whenever(prisoner.conditionalReleaseDate).thenReturn(LocalDate.of(2026, 8, 20))

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `should return false when conditional release date is null`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(LocalDate.of(2026, 8, 1))
    whenever(prisoner.conditionalReleaseDate).thenReturn(null)

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `should return false when current date is before home detention curfew actual date`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(LocalDate.of(2026, 8, 6))
    whenever(prisoner.conditionalReleaseDate).thenReturn(LocalDate.of(2026, 8, 20))

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `should return false when current date is after release event window`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(LocalDate.of(2026, 7, 1))
    whenever(prisoner.conditionalReleaseDate).thenReturn(LocalDate.of(2026, 8, 14))

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isFalse()
  }

  @Test
  fun `should return true when current date is equal to hdc actual date`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(LocalDate.of(2026, 8, 5))
    whenever(prisoner.conditionalReleaseDate).thenReturn(LocalDate.of(2026, 8, 20))

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isTrue()
  }

  @Test
  fun `should return true when current date is equal to conditional release date minus ten days`() {
    // Given
    service = createService()

    val prisoner = mock<Prisoner>()
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(LocalDate.of(2026, 8, 1))
    whenever(prisoner.conditionalReleaseDate).thenReturn(LocalDate.of(2026, 8, 15))

    // When
    val result = service.isWithinReleaseEventWindow(prisoner)

    // Then
    assertThat(result).isTrue()
  }

  private fun createService(
    allowedMigrationDate: LocalDate? = null,
    clock: Clock = this.clock,
  ) = MigrationProcessService(
    migrationRepository = migrationRepository,
    migrationRequestService = migrationRequestService,
    prisonSearchApiClient = prisonSearchApiClient,
    allowedBulkMigrationDate = allowedMigrationDate,
    clock = clock,
  )
}
