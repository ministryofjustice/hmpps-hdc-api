package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.AddressType
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.FirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
import java.time.DayOfWeek
import java.time.LocalTime

class LicenceServiceTest : SqsIntegrationTestBase() {

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Retrieve licence with an approved preferred address`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/12345")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(HdcLicence::class.java)
      .returnResult().responseBody

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "1 Test Street",
        "Test Area",
        "Test Town",
        null,
        "T33 3ST",
        AddressType.CAS,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        LocalTime.of(15, 0),
        LocalTime.of(7, 0),
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
    assertThat(result?.curfewTimes)
      .extracting<Tuple> {
        Tuple.tuple(
          it?.fromDay,
          it?.fromTime,
          it?.untilDay,
          it?.untilTime,
        )
      }
      .contains(
        Tuple.tuple(DayOfWeek.MONDAY, LocalTime.of(19, 0), DayOfWeek.TUESDAY, LocalTime.of(7, 0)),
        Tuple.tuple(DayOfWeek.TUESDAY, LocalTime.of(19, 0), DayOfWeek.WEDNESDAY, LocalTime.of(7, 0)),
        Tuple.tuple(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), DayOfWeek.THURSDAY, LocalTime.of(7, 0)),
        Tuple.tuple(DayOfWeek.THURSDAY, LocalTime.of(19, 0), DayOfWeek.FRIDAY, LocalTime.of(7, 0)),
        Tuple.tuple(DayOfWeek.FRIDAY, LocalTime.of(19, 0), DayOfWeek.SATURDAY, LocalTime.of(7, 0)),
        Tuple.tuple(DayOfWeek.SATURDAY, LocalTime.of(19, 0), DayOfWeek.SUNDAY, LocalTime.of(7, 0)),
        Tuple.tuple(DayOfWeek.SUNDAY, LocalTime.of(19, 0), DayOfWeek.MONDAY, LocalTime.of(7, 0)),
      )
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Retrieve licence with an approved CAS2 address`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/43210")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(HdcLicence::class.java)
      .returnResult().responseBody

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "2 Test Road",
        null,
        "Another Town",
        null,
        "TS7 7TS",
        AddressType.CAS,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        LocalTime.of(15, 0),
        LocalTime.of(7, 0),
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Retrieve licence with cas2 address`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/98765")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(HdcLicence::class.java)
      .returnResult().responseBody

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "100 CAS2 Street",
        "The Avenue",
        "Test City",
        null,
        "TS12 TST",
        AddressType.CAS,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        LocalTime.of(15, 0),
        LocalTime.of(7, 0),
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Retrieve licence with preferred address`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/54321")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(HdcLicence::class.java)
      .returnResult().responseBody

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "123 Approved Premises Street 2",
        "Off Test Place",
        "Test City",
        null,
        "TST1 1TS",
        AddressType.RESIDENTIAL,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        LocalTime.of(15, 0),
        LocalTime.of(7, 0),
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
  }

  @Test
  fun `Get forbidden (403) when incorrect roles are supplied`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/12345")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_VERY_WRONG")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.FORBIDDEN.value())
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).contains("Access Denied")
  }

  @Test
  fun `Unauthorized (401) when no token is supplied`() {
    webTestClient.get()
      .uri("/licence/hdc/12345")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED.value())
  }

  @Test
  fun `Get not found (404) when no licence is found`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/11111")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NOT_FOUND.value())
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).isEqualTo("Data not found: No licence found for booking id 11111")
  }

  @Test
  fun `Get not found (404) when no licence data is found`() {
    val result = webTestClient.get()
      .uri("/licence/hdc/22222")
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NOT_FOUND.value())
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody

    assertThat(result?.userMessage).isEqualTo("Data not found: No licence data found for booking id 22222")
  }

  private companion object {

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      hmppsAuthMockServer.stop()
    }
  }
}
