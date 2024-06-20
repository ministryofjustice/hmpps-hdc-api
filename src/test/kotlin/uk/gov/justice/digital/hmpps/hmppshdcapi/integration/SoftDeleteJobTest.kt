package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService.JobResponse
import java.time.LocalDate
import java.time.LocalDateTime

class SoftDeleteJobTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var licenceRepository: LicenceRepository

  @Autowired
  lateinit var licenceVersionRepository: LicenceVersionRepository

  @Autowired
  lateinit var auditEventRepository: AuditEventRepository

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/populate-deleted-at.sql",
  )
  fun `Perform job`() {
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
      listOf(
        Prisoner("A1234AA", "10", "MDI", topupSupervisionExpiryDate = LocalDate.now(), licenceExpiryDate = LocalDate.now().minusDays(1)),
        Prisoner("A1234CC", "30", "MDI", topupSupervisionExpiryDate = null, licenceExpiryDate = LocalDate.now()),
        Prisoner("A1234EE", "50", "MDI", topupSupervisionExpiryDate = null, licenceExpiryDate = null),
      ),
    )

    val result = webTestClient.post()
      .uri("/jobs/populate-deleted-at-for-licences/4")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf()))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(JobResponse::class.java)
      .returnResult().responseBody!!

    with(result) {
      assertThat(totalProcessed).isEqualTo(12)
      assertThat(totalFailedToProcess).isEqualTo(2)
      assertThat(batchSize).isEqualTo(4)
      assertThat(totalDeleted).isEqualTo(2)
      assertThat(totalBatches).isEqualTo(3)
    }

    val records = licenceRepository.findAll().sortedBy { it.bookingId }.associate { it.bookingId to it.deletedAt }
    val versions = licenceVersionRepository.findAll().sortedBy { it.id }.associate { it.id to it.deletedAt }

    assertThat(records.filterValues { it != null }).containsKeys(10L, 30L, 40L)
    assertThat(records.filterValues { it == null }).containsKeys(50L)
    // multiple versions of a licence are also soft deleted where appropriate
    // e.g. licence versions 13 and 14 of licence bookingId 30 deletedAt populated with timestamp and therefore not null
    assertThat(versions.filterValues { it != null }).containsKeys(11, 13, 14, 15, 16)
    assertThat(versions.filterValues { it == null }).containsKeys(12, 17, 18)

    // We don't re-delete previously deleted licences
    assertThat(versions[16]).isEqualTo(LocalDateTime.of(2022, 7, 27, 15, 0, 0, 0))
    assertThat(versions[11]!!.toLocalDate()).isEqualTo(LocalDate.now())

    val events = auditEventRepository.findAll()
    assertThat(events).hasSize(2)

    assertThat(events).extracting("user", "action", "details").containsExactly(
      Assertions.tuple("SYSTEM:JOB", "RESET", mapOf("bookingId" to 10)),
      Assertions.tuple("SYSTEM:JOB", "RESET", mapOf("bookingId" to 30)),
    )
  }

  private companion object {

    val prisonerSearchMockServer = PrisonerSearchMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonerSearchMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      hmppsAuthMockServer.stop()
      prisonerSearchMockServer.stop()
    }
  }
}
