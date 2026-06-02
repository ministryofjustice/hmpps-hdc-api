package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AddressAndPhone
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Cas2Offer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Curfew
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurrentCas2Referral
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Decision
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.OfferAccepted
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.ProposedAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.AddressType
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
    assertThat(result).isNotNull.hasSize(7)

    assertThat(result)
      .allSatisfy {
        assertThat(it.fromTime).isEqualTo(LocalTime.of(17, 0))
        assertThat(it.untilTime).isEqualTo(LocalTime.of(6, 0))
      }
    assertThat(result).extracting<DayOfWeek> { it.fromDay }.containsExactlyElementsOf(expectedOrderForFromDays)
    assertThat(result).extracting<DayOfWeek> { it.untilDay }.containsExactlyElementsOf(expectedOrderForUntilDays)
  }

  @Test
  fun whenDayNotSpecificAndAllFromAndAllUntilAreNotGivenThenResultsAreEmpty() {
    // Given
    val curfewHours = createCurfewHours(
      daySpecificInputs = Decision.NO,
    )

    // When
    val result = toMigrateCurfewTimes(curfewHours)

    // Then
    assertThat(result).isNotNull.isEmpty()
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
    assertThat(result).isNotNull.hasSize(7)

    assertThat(result)
      .allSatisfy {
        assertThat(it.fromTime).isEqualTo(LocalTime.of(12, 0))
        assertThat(it.untilTime).isEqualTo(LocalTime.of(17, 0))
      }
    assertThat(result).extracting<DayOfWeek> { it.fromDay }.containsExactlyElementsOf(expectedOrderForDays)
    assertThat(result).extracting<DayOfWeek> { it.untilDay }.containsExactlyElementsOf(expectedOrderForDays)
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
    assertThat(result).hasSize(7)
    assertThat(result).allSatisfy {
      assertThat(it.fromTime).isEqualTo(LocalTime.of(9, 0))
      assertThat(it.untilTime).isEqualTo(LocalTime.of(17, 0))
      assertThat(it.untilDay).isEqualTo(it.fromDay)
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
    assertThat(result).hasSize(1)
    assertThat(result.first().fromDay).isEqualTo(DayOfWeek.MONDAY)
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
    assertThat(result).hasSize(1)
    val curfew = result.first()
    assertThat(curfew.fromDay).isEqualTo(DayOfWeek.MONDAY)
    assertThat(curfew.untilDay).isEqualTo(DayOfWeek.TUESDAY)
    assertThat(curfew.fromTime).isEqualTo(LocalTime.of(22, 0))
    assertThat(curfew.untilTime).isEqualTo(LocalTime.of(6, 0))
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

    // When + Then
    migrationRequestService.validate(prisoner)
  }

  @Test
  fun `should return false when licence expiry date are null`() {
    // Given
    val prisoner = mock<Prisoner>()
    val today = LocalDate.now()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today.minusDays(1))
    whenever(prisoner.licenceExpiryDate).thenReturn(null)

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Missing licence expiry date")
  }

  @Test
  fun `should return false when hdcad date are null`() {
    // Given
    val prisoner = mock<Prisoner>()
    val today = LocalDate.now()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(null)
    whenever(prisoner.licenceExpiryDate).thenReturn(today.plusDays(1))

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Licence has missing HDCAD date")
  }

  @Test
  fun `should return false when licence expiry date is null`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today)

    whenever(prisoner.licenceExpiryDate).thenReturn(null)

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Missing licence expiry date")
  }

  @Test
  fun `should return false when status is not inactive out`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()

    whenever(prisoner.status).thenReturn("ACTIVE")
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today)
    whenever(prisoner.licenceExpiryDate).thenReturn(today)

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Licence has invalid status: ACTIVE")
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

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Licence has restricted patient")
  }

  @Test
  fun `should return false when hdcad is in the future`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()
    val hdcad = today.plusDays(1)

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(hdcad)
    whenever(prisoner.licenceExpiryDate).thenReturn(today.plusDays(1))

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Licence has HDCAD in the future: $hdcad")
  }

  @Test
  fun `should return false when licence expiry date is in the past`() {
    // Given
    val today = LocalDate.now()
    val prisoner = mock<Prisoner>()
    val led = today.minusDays(1)

    whenever(prisoner.status).thenReturn("INACTIVE OUT")
    whenever(prisoner.isRestrictedPatient()).thenReturn(false)
    whenever(prisoner.homeDetentionCurfewActualDate).thenReturn(today.minusDays(1))
    whenever(prisoner.licenceExpiryDate).thenReturn(led)

    // When
    assertThatThrownBy {
      migrationRequestService.validate(prisoner)
      // Then
    }.isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("Licence expiry date is in past: LED=$led")
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "  ", " "])
  fun `should return null when licence appointment time is blank or empty`(time: String?) {
    // Given
    val date = "20/05/2001"

    // When
    val result = migrationRequestService.toLocalDateTimeOrDate(date, time)

    // Then
    assertThat(result).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "  ", " "])
  fun `should return null when licence appointment date is blank or empty`(date: String?) {
    // Given
    val time = "10:30"

    // When
    val result = migrationRequestService.toLocalDateTimeOrDate(date, time)

    // Then
    assertThat(result).isNull()
  }

  @Test
  fun `should return null when licence appointment time is null`() {
    // Given
    val date = "20/05/2001"

    // When
    val result = migrationRequestService.toLocalDateTimeOrDate(date, null)

    // Then
    assertThat(result).isNull()
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "  ", " "])
  fun `should return null when licence appointment date is null`() {
    // Given
    val time = "10:30"

    // When
    val result = migrationRequestService.toLocalDateTimeOrDate(null, time)

    // Then
    assertThat(result).isNull()
  }

  @Test
  fun shouldMapCurfewApprovedPremisesAddressWhenPresent() {
    // Given
    val licenceData = baseLicenceData(
      curfew = Curfew(
        approvedPremisesAddress = addressAndPhone("FROM_CURFEW_APPROVED_PREMISES"),
        firstNight = null,
        curfewHours = null,
      ),
      bassReferral = CurrentCas2Referral(
        approvedPremisesAddress = addressAndPhone("FROM_BASS_APPROVED_PREMISES"),
        bassOffer = cas2Offer("FROM_BASS_OFFER"),
      ),
      proposedAddress = ProposedAddress(
        curfewAddress = curfewAddress("FROM_PROPOSED_RESIDENTIAL_ADDRESS"),
      ),
    )

    // When
    val result = migrationRequestService.mapCurfewAddress(licenceData)

    // Then
    assertThat(result.addressLine1).isEqualTo("FROM_CURFEW_APPROVED_PREMISES")
    assertThat(result.addressType).isEqualTo(AddressType.CAS)
  }

  @Test
  fun shouldMapBassApprovedPremisesAddressWhenCurfewApprovedPremisesMissing() {
    // Given
    val licenceData = baseLicenceData(
      bassReferral = CurrentCas2Referral(
        approvedPremisesAddress = addressAndPhone("FROM_BASS_APPROVED_PREMISES"),
        bassOffer = cas2Offer("FROM_BASS_OFFER"),
      ),
      proposedAddress = ProposedAddress(
        curfewAddress = curfewAddress("FROM_PROPOSED_RESIDENTIAL_ADDRESS"),
      ),
    )

    // When
    val result = migrationRequestService.mapCurfewAddress(licenceData)

    // Then
    assertThat(result.addressLine1).isEqualTo("FROM_BASS_APPROVED_PREMISES")
    assertThat(result.addressType).isEqualTo(AddressType.CAS)
  }

  @Test
  fun shouldMapProposedResidentialAddressWhenNoCasAddressesExist() {
    // Given
    val licenceData = baseLicenceData(
      proposedAddress = ProposedAddress(
        curfewAddress = curfewAddress("FROM_PROPOSED_RESIDENTIAL_ADDRESS"),
      ),
    )

    // When
    val result = migrationRequestService.mapCurfewAddress(licenceData)

    // Then
    assertThat(result.addressLine1).isEqualTo("FROM_PROPOSED_RESIDENTIAL_ADDRESS")
    assertThat(result.addressType).isEqualTo(AddressType.RESIDENTIAL)
  }

  @Test
  fun shouldMapBassOfferWhenNoHigherPriorityAddressesExist() {
    // Given
    val licenceData = baseLicenceData(
      bassReferral = CurrentCas2Referral(
        bassOffer = cas2Offer("FROM_BASS_OFFER"),
      ),
    )

    // When
    val result = migrationRequestService.mapCurfewAddress(licenceData)

    // Then
    assertThat(result.addressLine1).isEqualTo("FROM_BASS_OFFER")
    assertThat(result.addressType).isEqualTo(AddressType.CAS)
  }

  @Test
  fun shouldThrowWhenAllAddressesAreMissing() {
    // Given
    val licenceData = baseLicenceData()

    // When / Then
    assertThatThrownBy {
      migrationRequestService.mapCurfewAddress(licenceData)
    }
      .isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("No valid curfew address found")
  }

  @Test
  fun shouldThrowWhenAddressesAreBlankOrWhitespaceOnly() {
    // Given
    val blankAddressPhone = AddressAndPhone("   ", addressTown = "\t", postCode = "")
    val blankCurfewAddress = CurfewAddress("   ", addressTown = "\t", postCode = "")
    val blankCas2Offer = Cas2Offer("   ", addressTown = "\t", postCode = "", bassAccepted = OfferAccepted.YES)

    val licenceData = baseLicenceData(
      curfew = Curfew(
        approvedPremisesAddress = blankAddressPhone,
        firstNight = null,
        curfewHours = null,
      ),
      bassReferral = CurrentCas2Referral(
        approvedPremisesAddress = blankAddressPhone,
        bassOffer = blankCas2Offer,
      ),
      proposedAddress = ProposedAddress(
        curfewAddress = blankCurfewAddress,
      ),
    )

    // When / Then
    assertThatThrownBy {
      migrationRequestService.mapCurfewAddress(licenceData)
    }
      .isInstanceOf(MigrationValidationException::class.java)
      .hasMessage("No valid curfew address found")
  }

  @Test
  fun shouldTreatAddressAsValidWhenOnlyOneFieldIsPopulated() {
    // Given
    val licenceData = baseLicenceData(
      proposedAddress = ProposedAddress(
        curfewAddress = CurfewAddress(
          addressLine1 = null,
          addressTown = null,
          postCode = "POSTCODE_ONLY",
        ),
      ),
    )

    // When
    val result = migrationRequestService.mapCurfewAddress(licenceData)

    // Then
    assertThat(result.postcode).isEqualTo("POSTCODE_ONLY")
    assertThat(result.addressType).isEqualTo(AddressType.RESIDENTIAL)
  }

  private fun baseLicenceData(
    curfew: Curfew? = null,
    bassReferral: CurrentCas2Referral? = null,
    proposedAddress: ProposedAddress? = null,
  ) = LicenceData(
    curfew = curfew,
    bassReferral = bassReferral,
    proposedAddress = proposedAddress,
    eligibility = null,
    risk = null,
    reporting = null,
    victim = null,
    licenceConditions = null,
    document = null,
    approval = null,
    finalChecks = null,
  )

  private fun curfewAddress(addressLine1: String) = CurfewAddress(
    addressLine1 = addressLine1,
    addressTown = "TEST_TOWN",
    postCode = "TEST_POSTCODE",
  )

  private fun addressAndPhone(addressLine1: String) = AddressAndPhone(
    addressLine1 = addressLine1,
    addressTown = "TEST_TOWN",
    postCode = "TEST_POSTCODE",
  )

  private fun cas2Offer(addressLine1: String) = Cas2Offer(
    addressLine1 = addressLine1,
    addressTown = "TEST_TOWN",
    postCode = "TEST_POSTCODE",
    bassAccepted = OfferAccepted.YES,
  )

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
