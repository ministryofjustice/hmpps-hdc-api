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
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
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
        "T33 3ST",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "15:00",
        "07:00",
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
    assertThat(result?.curfewTimes)
      .extracting<Tuple> {
        Tuple.tuple(
          it?.licenceId,
          it?.fromDay,
          it?.fromTime,
          it?.untilDay,
          it?.untilTime,
        )
      }
      .contains(
        Tuple.tuple(3L, "Monday", LocalTime.of(19, 0), "Tuesday", LocalTime.of(7, 0)),
        Tuple.tuple(3L, "Tuesday", LocalTime.of(19, 0), "Wednesday", LocalTime.of(7, 0)),
        Tuple.tuple(3L, "Wednesday", LocalTime.of(19, 0), "Thursday", LocalTime.of(7, 0)),
        Tuple.tuple(3L, "Thursday", LocalTime.of(19, 0), "Friday", LocalTime.of(7, 0)),
        Tuple.tuple(3L, "Friday", LocalTime.of(19, 0), "Saturday", LocalTime.of(7, 0)),
        Tuple.tuple(3L, "Saturday", LocalTime.of(19, 0), "Sunday", LocalTime.of(7, 0)),
        Tuple.tuple(3L, "Sunday", LocalTime.of(19, 0), "Monday", LocalTime.of(7, 0)),
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
        "AB1 2CD",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "15:00",
        "07:00",
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
    assertThat(result?.curfewTimes)
      .extracting<Tuple> {
        Tuple.tuple(
          it?.licenceId,
          it?.fromDay,
          it?.fromTime,
          it?.untilDay,
          it?.untilTime,
        )
      }
      .contains(
        Tuple.tuple(4L, "Monday", LocalTime.of(19, 0), "Tuesday", LocalTime.of(7, 0)),
        Tuple.tuple(4L, "Tuesday", LocalTime.of(19, 0), "Wednesday", LocalTime.of(7, 0)),
        Tuple.tuple(4L, "Wednesday", LocalTime.of(19, 0), "Thursday", LocalTime.of(7, 0)),
        Tuple.tuple(4L, "Thursday", LocalTime.of(19, 0), "Friday", LocalTime.of(7, 0)),
        Tuple.tuple(4L, "Friday", LocalTime.of(19, 0), "Saturday", LocalTime.of(7, 0)),
        Tuple.tuple(4L, "Saturday", LocalTime.of(19, 0), "Sunday", LocalTime.of(7, 0)),
        Tuple.tuple(4L, "Sunday", LocalTime.of(19, 0), "Monday", LocalTime.of(7, 0)),
      )
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
        "Leeds",
        "LS3 4BB",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "15:00",
        "07:00",
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
    assertThat(result?.curfewTimes)
      .extracting<Tuple> {
        Tuple.tuple(
          it?.licenceId,
          it?.fromDay,
          it?.fromTime,
          it?.untilDay,
          it?.untilTime,
        )
      }
      .contains(
        Tuple.tuple(2L, "Monday", LocalTime.of(19, 0), "Tuesday", LocalTime.of(7, 0)),
        Tuple.tuple(2L, "Tuesday", LocalTime.of(19, 0), "Wednesday", LocalTime.of(7, 0)),
        Tuple.tuple(2L, "Wednesday", LocalTime.of(19, 0), "Thursday", LocalTime.of(7, 0)),
        Tuple.tuple(2L, "Thursday", LocalTime.of(19, 0), "Friday", LocalTime.of(7, 0)),
        Tuple.tuple(2L, "Friday", LocalTime.of(19, 0), "Saturday", LocalTime.of(7, 0)),
        Tuple.tuple(2L, "Saturday", LocalTime.of(19, 0), "Sunday", LocalTime.of(7, 0)),
        Tuple.tuple(2L, "Sunday", LocalTime.of(19, 0), "Monday", LocalTime.of(7, 0)),
      )
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
        "Off St Michaels Place",
        "Leeds",
        "LS1 2AA",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "15:00",
        "07:00",
      ),
    )
    assertThat(result?.curfewTimes?.size).isEqualTo(7)
    assertThat(result?.curfewTimes)
      .extracting<Tuple> {
        Tuple.tuple(
          it?.licenceId,
          it?.fromDay,
          it?.fromTime,
          it?.untilDay,
          it?.untilTime,
        )
      }
      .contains(
        Tuple.tuple(1L, "Monday", LocalTime.of(19, 0), "Tuesday", LocalTime.of(7, 0)),
        Tuple.tuple(1L, "Tuesday", LocalTime.of(19, 0), "Wednesday", LocalTime.of(7, 0)),
        Tuple.tuple(1L, "Wednesday", LocalTime.of(19, 0), "Thursday", LocalTime.of(7, 0)),
        Tuple.tuple(1L, "Thursday", LocalTime.of(19, 0), "Friday", LocalTime.of(7, 0)),
        Tuple.tuple(1L, "Friday", LocalTime.of(19, 0), "Saturday", LocalTime.of(7, 0)),
        Tuple.tuple(1L, "Saturday", LocalTime.of(19, 0), "Sunday", LocalTime.of(7, 0)),
        Tuple.tuple(1L, "Sunday", LocalTime.of(19, 0), "Monday", LocalTime.of(7, 0)),
      )
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
