package uk.gov.justice.digital.hmpps.hmppshdcapi.softdelete

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.AuditEventType
import java.time.LocalDate
import java.time.LocalDateTime

class SoftDeleteServiceTest {
  private val licenceRepository = mock<LicenceRepository>()
  private val licenceVersionRepository = mock<LicenceVersionRepository>()
  private val auditEventRepository = mock<AuditEventRepository>()
  private val prisonSearchApiClient = mock<PrisonSearchApiClient>()

  private val service =
    SoftDeleteService(
      licenceRepository,
      licenceVersionRepository,
      auditEventRepository,
      prisonSearchApiClient,
    )

  private val today = LocalDate.of(2024, 4, 16)

  @BeforeEach
  fun reset() {
    reset(licenceRepository, licenceVersionRepository, auditEventRepository, prisonSearchApiClient)
  }

  @Test
  fun `is to be soft deleted when TUSED is before LED and LED is today`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = today.minusDays(1), licenceExpiryDate = today)
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is before LED and LED is in the past`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = today.minusDays(2), licenceExpiryDate = today.minusDays(1))
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is today`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = today)
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is in the past`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = today.minusDays(1))
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is null but LED is today`() {
    val prisoner = prisoner.copy(licenceExpiryDate = today)
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  @Test
  fun `is to be soft deleted when TUSED is null but LED is in the past`() {
    val prisoner = prisoner.copy(licenceExpiryDate = today.minusDays(1))
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  @Test
  fun `records an audit event when licence is soft deleted`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = today.minusDays(1), licenceExpiryDate = today)
    val auditEvent = AuditEvent(user = "SYSTEM_JOB", action = "RESET", timestamp = LocalDateTime.now(), details = mapOf("bookingId" to "1"))
    val result = service.isToBeSoftDeleted(prisoner)

    whenever(auditEventRepository.save(auditEvent)).thenReturn(auditEvent)
    service.applyAnySoftDeletes(PageImpl(listOf(Pair(licence, prisoner))), AuditEventType.SYSTEM_JOB.eventType)
    assertThat(result).isTrue
    verify(auditEventRepository, times(1)).save(auditEvent)
  }

  @Test
  fun `is not to be soft deleted when both TUSED and LED are null`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = null, licenceExpiryDate = null)
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isFalse
  }

  private companion object {
    val prisoner = Prisoner(
      prisonerNumber = "A1234BC",
      bookingId = "1",
      prisonId = "MDI",
      topupSupervisionExpiryDate = null,
      licenceExpiryDate = null,
    )

    val licence = Licence(
      id = 1L,
      prisonNumber = "A1234BC",
      bookingId = 1L,
      stage = "ELIGIBILITY",
      version = 1,
      transitionDate = LocalDateTime.of(2024, 3, 16, 12, 0),
      varyVersion = 0,
      deletedAt = null,
      additionalConditionsVersion = null,
      standardConditionsVersion = null,
      licence = null,
    )

    val licenceVersion = LicenceVersion(
      id = 1L,
      prisonNumber = "A1234BC",
      bookingId = 1L,
      timestamp = LocalDateTime.of(2024, 3, 16, 12, 0),
      version = 1,
      template = "hdc_ap",
      varyVersion = 0,
      deletedAt = null,
      licence = null,
    )
  }
}
