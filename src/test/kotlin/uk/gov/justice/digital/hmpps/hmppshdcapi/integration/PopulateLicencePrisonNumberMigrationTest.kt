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
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.PopulateLicencePrisonNumberMigration
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.UNKNOWN_PRISON_NUMBER_BY_PRISON_API
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking

class PopulateLicencePrisonNumberMigrationTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var licenceRepository: LicenceRepository

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/populate-prison-number.sql",
  )
  fun `Perform migration`() {
    prisonApiMockServer.stubGetByBookingId(Booking("A1234BB", 10L, "MDI"))
    prisonApiMockServer.stubGetByBookingId(Booking("A1234CC", 40L, "MDI"))

    val result = webTestClient.post()
      .uri("/migrations/populate-prison-numbers-for-licences/3")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_$ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(PopulateLicencePrisonNumberMigration.Response::class.java)
      .returnResult().responseBody!!

    with(result) {
      assertThat(migrateSuccess).isEqualTo(2)
      assertThat(migrateFail).isEqualTo(1)
      assertThat(batchSize).isEqualTo(3)
      assertThat(totalBatches).isEqualTo(2)
      assertThat(totalRemaining).isEqualTo(1)
    }

    val records = licenceRepository.findAll().sortedBy { it.bookingId }.map { it.bookingId to it.prisonNumber }

    assertThat(records).containsExactly(
      10L to "A1234BB",
      // Previously populated in DB
      20L to "A1234AA",
      // Not found in prison Api
      30L to UNKNOWN_PRISON_NUMBER_BY_PRISON_API,
      40L to "A1234CC",
      // Not picked up due to limit of 3
      50L to "???",
    )
  }

  @Test
  fun `Get forbidden (403) when incorrect roles are supplied`() {
    val result = webTestClient.post()
      .uri("/migrations/populate-prison-numbers-for-licences/3")
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
      .uri("/migrations/populate-prison-numbers-for-licences/3")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED.value())
  }

  private companion object {

    val prisonApiMockServer = PrisonApiMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      hmppsAuthMockServer.stop()
      prisonApiMockServer.stop()
    }
  }
}
