package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.softdelete.SoftDeleteService.MigrationBatchResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.LocalDate
import java.time.LocalDateTime

class SoftDeleteMigrationTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var licenceRepository: LicenceRepository

  @Autowired
  lateinit var licenceVersionRepository: LicenceVersionRepository

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/populate-deleted-at.sql",
  )
  fun `Perform migration`() {
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
      listOf(
        Prisoner("A1234AA", "10", "MDI", topupSupervisionExpiryDate = LocalDate.now(), licenceExpiryDate = LocalDate.now().minusDays(1)),
        Prisoner("A1234CC", "30", "MDI", topupSupervisionExpiryDate = null, licenceExpiryDate = LocalDate.now()),
        Prisoner("A1234EE", "50", "MDI", topupSupervisionExpiryDate = null, licenceExpiryDate = null),
      ),
    )

    val result = webTestClient.post()
      .uri("/migrations/populate-deleted-at-for-licences/0/4")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_$ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(MigrationBatchResponse::class.java)
      .returnResult().responseBody!!

    with(result) {
      assertThat(totalProcessed).isEqualTo(4)
      assertThat(totalFailedToProcess).isEqualTo(1)
      assertThat(batchSize).isEqualTo(4)
      assertThat(totalDeleted).isEqualTo(2)
      assertThat(lastIdProcessed).isEqualTo(5)
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
  }

  @Test
  fun `Get forbidden (403) when incorrect roles are supplied`() {
    val result = webTestClient.post()
      .uri("/migrations/populate-deleted-at-for-licences/0/3")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_VERY_WRONG_ROLE")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN.value())
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).contains("Access Denied")
  }

  @Test
  fun `Unauthorized (401) when no token is supplied`() {
    webTestClient.get()
      .uri("/migrations/populate-deleted-at-for-licences/0/3")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED.value())
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
