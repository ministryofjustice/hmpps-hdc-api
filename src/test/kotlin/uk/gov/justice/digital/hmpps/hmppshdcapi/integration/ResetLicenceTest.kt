package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_HDC_ADMIN
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ROLE_SAR_DATA_ACCESS
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate
import java.time.LocalDateTime

class ResetLicenceTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var auditEventRepository: AuditEventRepository

  @Autowired
  lateinit var licenceRepository: LicenceRepository

  private fun jsonFromFile(name: String) =
    this.javaClass.getResourceAsStream("/test_data/responses/$name")!!.bufferedReader(UTF_8).readText()

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/reset-licences.sql",
  )
  fun `check reset request`() {
    webTestClient.post()
      .uri("/licences/reset")
      .bodyValue(listOf(10, 30, 40, 64))
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_${ROLE_HDC_ADMIN}")))
      .exchange()
      .expectStatus().isOk
      .expectBody().json(jsonFromFile("reset-request.json"), true)

    val events = auditEventRepository.findAll()
    assertThat(events).hasSize(3)

    assertThat(events).extracting("user", "action", "details").containsExactly(
      tuple("SYSTEM:API", "RESET", mapOf("bookingId" to 10)),
      tuple("SYSTEM:API", "RESET", mapOf("bookingId" to 30)),
      tuple("SYSTEM:API", "RESET", mapOf("bookingId" to 40)),
    )

    val licences = licenceRepository.findAll().sortedBy { it.bookingId }
    assertThat(licences).hasSize(6)

    assertThat(licences).extracting("bookingId", "deletedAt")
      .map<Pair<Long, LocalDateTime?>> { it.toList().let { (bookingId, dateTime) -> bookingId as Long to dateTime as LocalDateTime? } }
      .map<Pair<Long, LocalDate?>> { (bookingId, dateTime) -> bookingId to dateTime?.toLocalDate() }
      .containsExactly(
        10L to LocalDate.of(2024, 6, 6),
        20L to null,
        30L to LocalDate.of(2024, 6, 6),
        40L to LocalDate.of(2022, 7, 27),
        40L to LocalDate.of(2024, 6, 6),
        50L to null,
      )
  }

  @Test
  fun `Get forbidden (403) when incorrect roles are supplied`() {
    val result = webTestClient.post()
      .uri("/licences/reset")
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
    webTestClient.post()
      .uri("/licences/reset")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED.value())
  }

}
