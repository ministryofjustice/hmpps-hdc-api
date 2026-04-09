package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Decision
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatusService
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceService
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import java.time.DayOfWeek
import java.time.LocalTime

class MigrationServiceTest {

  private lateinit var migrationService: MigrationService
  private var migrationRepository: MigrationRepository = mock()
  private var cvlClient: CvlApiClient = mock()
  private var prisonApiClient: PrisonApiClient = mock()
  private var prisonSearchApiClient: PrisonSearchApiClient = mock()
  private var auditEventRepository: AuditEventRepository = mock()
  private var hdcStatusService: HdcStatusService = mock()
  private var licenceService: LicenceService = mock()

  @BeforeEach
  fun setUp() {
    migrationService = MigrationService(
      migrationRepository = migrationRepository,
      cvlClient = cvlClient,
      prisonApiClient = prisonApiClient,
      prisonSearchApiClient = prisonSearchApiClient,
      auditEventRepository = auditEventRepository,
      hdcStatusService = hdcStatusService,
      licenceService = licenceService,
    )
  }

  @Test
  fun shouldReturnAllSameCurfewTimeWhenNotDaySpecific() {
    // Given
    val curfewHours = createCurfewHours(
      allFrom = LocalTime.of(9, 0),
      allUntil = LocalTime.of(17, 0),
      mondayFrom = LocalTime.of(9, 0),
      mondayUntil = LocalTime.of(17, 0),
      daySpecificInputs = Decision.NO,
    )

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    Assertions.assertThat(result).hasSize(1)
    val curfew = result.first()
    Assertions.assertThat(curfew.fromTime).isEqualTo(LocalTime.of(9, 0))
    Assertions.assertThat(curfew.untilTime).isEqualTo(LocalTime.of(17, 0))
    Assertions.assertThat(curfew.fromDay).isNull()
    Assertions.assertThat(curfew.untilDay).isNull()
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

  private fun toMigrateCurfewTimes(curfewHours: CurfewHours): List<MigrateCurfewTime> = migrationService.toMigrateCurfewTimes(curfewHours)

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
