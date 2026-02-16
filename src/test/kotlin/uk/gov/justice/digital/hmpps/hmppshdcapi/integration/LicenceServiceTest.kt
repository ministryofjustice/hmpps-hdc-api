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
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.AddressType
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.FirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class LicenceServiceTest : SqsIntegrationTestBase() {

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Retrieve licence with an approved preferred address`() {
    stubPrisonersAndHdc(12345L, "APPROVED")
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
    assertThat(result?.status).isEqualTo(HdcStatus.APPROVED)
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
    stubPrisonersAndHdc(43210L, "ELIGIBILITY")
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
    assertThat(result?.status).isEqualTo(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)
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
    stubPrisonersAndHdc(98765L, "REJECTED")
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
    assertThat(result?.status).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
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
    stubPrisonersAndHdc(54321L, "OTHER")
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
    assertThat(result?.status).isEqualTo(HdcStatus.NOT_STARTED)
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

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Bulk HDC statuses returns statuses for all available booking ids`() {
    val requestBody = listOf(12345L, 54321L, 98765L)

    prisonApiMockServer.getHdcStatuses(listOf(12345L to "APPROVED", 54321L to "OTHER", 98765L to "REJECTED"))
    prisonerSearchMockServer.stubSearchPrisonersByBookingIdsList(requestBody)

    val result = webTestClient.post()
      .uri("/licence/hdc/status")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(uk.gov.justice.digital.hmpps.hmppshdcapi.model.BookingHdcStatus::class.java)
      .returnResult().responseBody

    assertThat(result).isNotNull
    assertThat(result?.size).isEqualTo(3)

    val mapById = result!!.associateBy { it.bookingId }
    assertThat(mapById[12345L]?.status).isEqualTo(HdcStatus.APPROVED)
    assertThat(mapById[54321L]?.status).isEqualTo(HdcStatus.NOT_STARTED)
    assertThat(mapById[98765L]?.status).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-licences.sql",
  )
  fun `Bulk HDC statuses should not omit booking ids with no licence data`() {
    val requestBody = listOf(12345L, 11111L)

    prisonApiMockServer.getHdcStatuses(listOf(12345L to "APPROVED"))
    prisonerSearchMockServer.stubSearchPrisonersByBookingIdsList(listOf(requestBody.first()))

    val result = webTestClient.post()
      .uri("/licence/hdc/status")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .bodyValue(requestBody)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(uk.gov.justice.digital.hmpps.hmppshdcapi.model.BookingHdcStatus::class.java)
      .returnResult().responseBody

    assertThat(result).isNotNull
    assertThat(result?.size).isEqualTo(2)
    assertThat(result?.first()?.bookingId).isEqualTo(12345L)
    assertThat(result?.first()?.status).isEqualTo(HdcStatus.APPROVED)
    assertThat(result?.last()?.bookingId).isEqualTo(11111L)
    assertThat(result?.last()?.status).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  private companion object {
    val prisonerSearchMockServer = PrisonerSearchMockServer()
    val prisonApiMockServer = PrisonApiMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonerSearchMockServer.start()
      prisonApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      hmppsAuthMockServer.stop()
      prisonerSearchMockServer.stop()
      prisonApiMockServer.stop()
    }

    private fun stubPrisonersAndHdc(bookingId: Long, approvalStatus: String) {
      prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
        listOf(
          Prisoner(
            prisonerNumber = "A1234AA",
            bookingId = bookingId.toString(),
            prisonId = "MDI",
            topupSupervisionExpiryDate = LocalDate.now(),
            licenceExpiryDate = LocalDate.now().minusDays(1),
            homeDetentionCurfewEligibilityDate = LocalDate.now().minusDays(2),
          ),
          Prisoner("A1234CC", "30", "MDI", topupSupervisionExpiryDate = null, licenceExpiryDate = LocalDate.now(), homeDetentionCurfewEligibilityDate = LocalDate.now().minusDays(2)),
          Prisoner("A1234EE", "50", "MDI", topupSupervisionExpiryDate = null, licenceExpiryDate = null, homeDetentionCurfewEligibilityDate = LocalDate.now().minusDays(2)),
        ),
      )
      prisonApiMockServer.getHdcStatuses(listOf(bookingId to approvalStatus))
    }
  }
}
