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
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.MIGRATION_ROLE
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.PopulateLicenceVersionPrisonNumberMigration
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.UNKNOWN_PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner

class PopulateLicenceVersionPrisonNumberMigrationTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var licenceVersionRepository: LicenceVersionRepository

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/populate-prison-number.sql",
  )
  fun `Perform migration`() {
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
      listOf(
        Prisoner("A1234BB", "10", "MDI"),
        Prisoner("A1234CC", "40", "MDI"),
      ),
    )

    val result = webTestClient.post()
      .uri("/migrations/populate-prison-numbers-for-licence-versions/3")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_$MIGRATION_ROLE")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PopulateLicenceVersionPrisonNumberMigration.Response::class.java)
      .returnResult().responseBody!!

    with(result) {
      assertThat(migrateSuccess).isEqualTo(2)
      assertThat(migrateFail).isEqualTo(1)
      assertThat(batchSize).isEqualTo(3)
      assertThat(totalBatches).isEqualTo(2)
      assertThat(totalRemaining).isEqualTo(1)
    }

    val records = licenceVersionRepository.findAll().sortedBy { it.bookingId }.map { it.bookingId to it.prisonNumber }

    assertThat(records).containsExactly(
      10L to "A1234BB",
      20L to "A1234AA", // Previously populated in DB
      30L to UNKNOWN_PRISON_NUMBER, // Not found in prisoner offender search
      40L to "A1234CC",
      50L to null, // Not picked up due to limit of 3
    )
  }

  @Test
  fun `Get forbidden (403) when incorrect roles are supplied`() {
    val result = webTestClient.post()
      .uri("/migrations/populate-prison-numbers-for-licence-versions/3")
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
      .uri("/migrations/populate-prison-numbers-for-licence-versions/3")
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
