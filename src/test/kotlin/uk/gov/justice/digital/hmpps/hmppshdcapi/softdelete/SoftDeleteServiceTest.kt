package uk.gov.justice.digital.hmpps.hmppshdcapi.softdelete

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository.LicenceIdentifiers
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService.JobResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.AuditEventType
import java.time.LocalDate
import java.time.LocalDateTime

class SoftDeleteServiceTest {
  private val licenceRepository = mock<LicenceRepository>()
  private val licenceVersionRepository = mock<LicenceVersionRepository>()
  private val auditEventRepository = mock<AuditEventRepository>()
  private val prisonSearchApiClient = mock<PrisonSearchApiClient>()
  private val transactionTemplate = mock<TransactionTemplate>()
  private val entityManager = mock<EntityManager>()

  private val service =
    SoftDeleteService(
      licenceRepository,
      licenceVersionRepository,
      auditEventRepository,
      prisonSearchApiClient,
      transactionTemplate,
      entityManager,
    )

  private val today = LocalDate.of(2024, 4, 16)

  @BeforeEach
  fun reset() {
    reset(licenceRepository, licenceVersionRepository, auditEventRepository, prisonSearchApiClient, transactionTemplate, entityManager)
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
    val auditEvent = AuditEvent(user = "SYSTEM:JOB", action = "RESET", timestamp = LocalDateTime.now(), details = mapOf("bookingId" to "1"))
    val result = service.isToBeSoftDeleted(prisoner)

    whenever(auditEventRepository.save(auditEvent)).thenReturn(auditEvent)
    service.applyAnySoftDeletes(PageImpl(listOf(Pair(licence, prisoner))), AuditEventType.SYSTEM_JOB)
    assertThat(result).isTrue
    verify(auditEventRepository, times(1)).save(auditEvent)
  }

  @Test
  fun `is not to be soft deleted when both TUSED and LED are null`() {
    val prisoner = prisoner.copy(topupSupervisionExpiryDate = null, licenceExpiryDate = null)
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isFalse
  }

  @Test
  fun `runJob`() {
    whenever(transactionTemplate.execute<Any>(any())).thenAnswer {
      (it.arguments[0] as TransactionCallback<*>).doInTransaction(SimpleTransactionStatus())
    }
    val prisonerPastLicenceEndDate = prisoner.copy(topupSupervisionExpiryDate = today.minusDays(1), licenceExpiryDate = today)

    val firstBatch = listOf(
      newLicence(1111) to prisonerPastLicenceEndDate.copy(bookingId = "1111"),
      newLicence(2222) to prisonerPastLicenceEndDate.copy(bookingId = "2222"),
    )
    val secondBatch = listOf(
      newLicence(3333) to prisonerPastLicenceEndDate.copy(bookingId = "3333"),
      newLicence(4444) to prisonerPastLicenceEndDate.copy(bookingId = "4444"),
    )

    whenever(licenceRepository.findAllByIdGreaterThanLastProcessed(any(), any())).thenReturn(PageImpl(firstBatch.map { it.first })).thenReturn(PageImpl(secondBatch.map { it.first })).thenReturn(Page.empty())
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(firstBatch.map { it.second }).thenReturn(secondBatch.map { it.second })
    whenever(auditEventRepository.save(AuditEvent(user = "SYSTEM:JOB", action = "RESET", timestamp = LocalDateTime.now(), details = mapOf("bookingId" to "1")))).thenReturn(AuditEvent(user = "SYSTEM:JOB", action = "RESET", timestamp = LocalDateTime.now(), details = mapOf("bookingId" to "1")))

    val result = service.runJob(batchSize = 2)

    assertThat(result.get()).isEqualTo(JobResponse(totalProcessed = 4, totalDeleted = 4, totalFailedToProcess = 0, batchSize = 2, totalBatches = 3))
    verify(transactionTemplate, times(3)).execute(any<TransactionCallback<Any>>())
  }

  private companion object {
    val prisoner = Prisoner(
      prisonerNumber = "A1234BC",
      bookingId = "1",
      prisonId = "MDI",
      topupSupervisionExpiryDate = null,
      licenceExpiryDate = null,
    )

    val newLicence = { bookingId: Long ->
      LicenceIdentifiers(
        id = 1L,
        prisonNumber = "A1234BC",
        bookingId = bookingId,
      )
    }
    val licence = newLicence(1L)
  }
}
