package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Decision
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class MigrationRequestServiceTest {

  private lateinit var migrationRequestService: MigrationRequestService
  private var migrationRepository: MigrationRepository = mock()
  private var cvlClient: CvlApiClient = mock()
  private var prisonApiClient: PrisonApiClient = mock()
  private var prisonSearchApiClient: PrisonSearchApiClient = mock()
  private var auditEventRepository: AuditEventRepository = mock()

  @BeforeEach
  fun setUp() {
    migrationRequestService = MigrationRequestService(
      migrationRepository = migrationRepository,
      cvlClient = cvlClient,
      prisonApiClient = prisonApiClient,
      prisonSearchApiClient = prisonSearchApiClient,
      auditEventRepository = auditEventRepository,
    )
  }

  @Test
  fun whenDayNotSpecificAndAllFromTimeAreAfterUtilTimesThenResultsAreReturnedAsExpected() {
    // Given
    val curfewHours = createCurfewHours(
      allFrom = LocalTime.of(17, 0),
      allUntil = LocalTime.of(6, 0),
      mondayFrom = LocalTime.of(9, 0),
      mondayUntil = LocalTime.of(21, 0),
      daySpecificInputs = Decision.NO,
    )

    val expectedOrderForFromDays = DayOfWeek.entries.toList()
    val expectedOrderForUntilDays = expectedOrderForFromDays.map { it.plus(1) }

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    Assertions.assertThat(result).isNotNull.hasSize(7)

    Assertions.assertThat(result)
      .allSatisfy {
        Assertions.assertThat(it.fromTime).isEqualTo(LocalTime.of(17, 0))
        Assertions.assertThat(it.untilTime).isEqualTo(LocalTime.of(6, 0))
      }
    Assertions.assertThat(result).extracting<DayOfWeek> { it.fromDay }.containsExactlyElementsOf(expectedOrderForFromDays)
    Assertions.assertThat(result).extracting<DayOfWeek> { it.untilDay }.containsExactlyElementsOf(expectedOrderForUntilDays)
  }

  @Test
  fun whenNotDaySpecificAndAllFromTimeAreBeforeUtilTimesThenResultsAreReturnedAsExpected() {
    // Given
    val curfewHours = createCurfewHours(
      allFrom = LocalTime.of(12, 0),
      allUntil = LocalTime.of(17, 0),
      mondayFrom = LocalTime.of(9, 0),
      mondayUntil = LocalTime.of(21, 0),
      daySpecificInputs = Decision.NO,
    )

    val expectedOrderForDays = DayOfWeek.entries.toList()

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    Assertions.assertThat(result).isNotNull.hasSize(7)

    Assertions.assertThat(result)
      .allSatisfy {
        Assertions.assertThat(it.fromTime).isEqualTo(LocalTime.of(12, 0))
        Assertions.assertThat(it.untilTime).isEqualTo(LocalTime.of(17, 0))
      }
    Assertions.assertThat(result).extracting<DayOfWeek> { it.fromDay }.containsExactlyElementsOf(expectedOrderForDays)
    Assertions.assertThat(result).extracting<DayOfWeek> { it.untilDay }.containsExactlyElementsOf(expectedOrderForDays)
  }

  @Test
  fun shouldMapAllDaysWhenDaySpecificAndTimesExist() {
    // Given
    val curfewHours = createCurfewHours(
      mondayFrom = LocalTime.of(9, 0), mondayUntil = LocalTime.of(17, 0),
      tuesdayFrom = LocalTime.of(9, 0), tuesdayUntil = LocalTime.of(17, 0),
      wednesdayFrom = LocalTime.of(9, 0), wednesdayUntil = LocalTime.of(17, 0),
      thursdayFrom = LocalTime.of(9, 0), thursdayUntil = LocalTime.of(17, 0),
      fridayFrom = LocalTime.of(9, 0), fridayUntil = LocalTime.of(17, 0),
      saturdayFrom = LocalTime.of(9, 0), saturdayUntil = LocalTime.of(17, 0),
      sundayFrom = LocalTime.of(9, 0), sundayUntil = LocalTime.of(17, 0),
      daySpecificInputs = Decision.YES,
    )

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    Assertions.assertThat(result).hasSize(7)
    Assertions.assertThat(result).allSatisfy {
      Assertions.assertThat(it.fromTime).isEqualTo(LocalTime.of(9, 0))
      Assertions.assertThat(it.untilTime).isEqualTo(LocalTime.of(17, 0))
      Assertions.assertThat(it.untilDay).isEqualTo(it.fromDay)
    }
  }

  @Test
  fun shouldSkipDaysWhenEitherFromOrUntilIsMissing() {
    // Given
    val curfewHours = createCurfewHours(
      mondayFrom = LocalTime.of(9, 0),
      mondayUntil = LocalTime.of(17, 0),
      tuesdayFrom = LocalTime.of(9, 0),
      tuesdayUntil = null,
      daySpecificInputs = Decision.YES,
    )

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result.first().fromDay).isEqualTo(DayOfWeek.MONDAY)
  }

  @Test
  fun shouldSetUntilDayToNextDayWhenCrossingMidnight() {
    // Given
    val curfewHours = createCurfewHours(
      mondayFrom = LocalTime.of(22, 0),
      mondayUntil = LocalTime.of(6, 0),
      daySpecificInputs = Decision.YES,
    )

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    Assertions.assertThat(result).hasSize(1)
    val curfew = result.first()
    Assertions.assertThat(curfew.fromDay).isEqualTo(DayOfWeek.MONDAY)
    Assertions.assertThat(curfew.untilDay).isEqualTo(DayOfWeek.TUESDAY)
    Assertions.assertThat(curfew.fromTime).isEqualTo(LocalTime.of(22, 0))
    Assertions.assertThat(curfew.untilTime).isEqualTo(LocalTime.of(6, 0))
  }

  @Test
  fun `should return true when prisoner is eligible`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today.minusDays(1))
    whenever(prisoner.licenceExpiryDate).thenReturn(today.plusDays(1))
    whenever(prisoner.topupSupervisionExpiryDate).thenReturn(today.plusDays(1))

    // When
    val result = migrationRequestService.isEligible(prisoner)

    // Then
    Assertions.assertThat(result).isTrue()
  }

  @Test
  fun `should return false when any required date is null`() {
    // Given
    val today = LocalDate.now()

    val prisonerWithNullHdcad = mock<Prisoner>()
    whenever(prisonerWithNullHdcad.homeDetentionCurfewActualDate).thenReturn(null)

    val prisonerWithNullLed = mock<Prisoner>()
    whenever(prisonerWithNullLed.homeDetentionCurfewActualDate).thenReturn(today)
    whenever(prisonerWithNullLed.licenceExpiryDate).thenReturn(null)

    val prisonerWithNullTused = mock<Prisoner>()
    whenever(prisonerWithNullTused.homeDetentionCurfewActualDate).thenReturn(today)
    whenever(prisonerWithNullTused.licenceExpiryDate).thenReturn(today)
    whenever(prisonerWithNullTused.topupSupervisionExpiryDate).thenReturn(null)

    // When / Then
    Assertions.assertThat(migrationRequestService.isEligible(prisonerWithNullHdcad)).isFalse()
    Assertions.assertThat(migrationRequestService.isEligible(prisonerWithNullLed)).isFalse()
    Assertions.assertThat(migrationRequestService.isEligible(prisonerWithNullTused)).isFalse()
  }

  @Test
  fun `should return false when status is not inactive out`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("ACTIVE")
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today)
    whenever(prisoner.licenceExpiryDate).thenReturn(today)
    whenever(prisoner.topupSupervisionExpiryDate).thenReturn(today)

    // When
    val result = migrationRequestService.isEligible(prisoner)

    // Then
    Assertions.assertThat(result).isFalse()
  }

  @Test
  fun `should return false when prisoner is restricted patient`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(true)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today)
    whenever(prisoner.licenceExpiryDate).thenReturn(today)
    whenever(prisoner.topupSupervisionExpiryDate).thenReturn(today)

    // When
    val result = migrationRequestService.isEligible(prisoner)

    // Then
    Assertions.assertThat(result).isFalse()
  }

  @Test
  fun `should return false when HDCAD is in the future`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today.plusDays(1))
    whenever(prisoner.licenceExpiryDate).thenReturn(today.plusDays(1))
    whenever(prisoner.topupSupervisionExpiryDate).thenReturn(today.plusDays(1))

    // When
    val result = migrationRequestService.isEligible(prisoner)

    // Then
    Assertions.assertThat(result).isFalse()
  }

  @Test
  fun `should return false when both licence and topup dates are in the past`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today.minusDays(1))
    whenever(prisoner.licenceExpiryDate).thenReturn(today.minusDays(1))
    whenever(prisoner.topupSupervisionExpiryDate).thenReturn(today.minusDays(1))

    // When
    val result = migrationRequestService.isEligible(prisoner)

    // Then
    Assertions.assertThat(result).isFalse()
  }

  @Test
  fun `should return true when only one of licence or topup is in the past`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today.minusDays(1))
    whenever(prisoner.licenceExpiryDate).thenReturn(today.minusDays(1))
    whenever(prisoner.topupSupervisionExpiryDate).thenReturn(today.plusDays(1))

    // When
    val result = migrationRequestService.isEligible(prisoner)

    // Then
    Assertions.assertThat(result).isTrue()
  }

  private fun toMigrateCurfewTimes(curfewHours: CurfewHours): List<MigrateCurfewTime> = migrationRequestService.toMigrateCurfewTimes(curfewHours)

  fun createCurfewHours(
    mondayFrom: LocalTime? = null,
    mondayUntil: LocalTime? = null,
    tuesdayFrom: LocalTime? = null,
    tuesdayUntil: LocalTime? = null,
    wednesdayFrom: LocalTime? = null,
    wednesdayUntil: LocalTime? = null,
    thursdayFrom: LocalTime? = null,
    thursdayUntil: LocalTime? = null,
    fridayFrom: LocalTime? = null,
    fridayUntil: LocalTime? = null,
    saturdayFrom: LocalTime? = null,
    saturdayUntil: LocalTime? = null,
    sundayFrom: LocalTime? = null,
    sundayUntil: LocalTime? = null,
    allFrom: LocalTime? = null,
    allUntil: LocalTime? = null,
    daySpecificInputs: Decision? = null,
  ): CurfewHours = CurfewHours(
    mondayFrom = mondayFrom, mondayUntil = mondayUntil,
    tuesdayFrom = tuesdayFrom, tuesdayUntil = tuesdayUntil,
    wednesdayFrom = wednesdayFrom, wednesdayUntil = wednesdayUntil,
    thursdayFrom = thursdayFrom, thursdayUntil = thursdayUntil,
    fridayFrom = fridayFrom, fridayUntil = fridayUntil,
    saturdayFrom = saturdayFrom, saturdayUntil = saturdayUntil,
    sundayFrom = sundayFrom, sundayUntil = sundayUntil,
    allFrom = allFrom, allUntil = allUntil,
    daySpecificInputs = daySpecificInputs,
  )
}
