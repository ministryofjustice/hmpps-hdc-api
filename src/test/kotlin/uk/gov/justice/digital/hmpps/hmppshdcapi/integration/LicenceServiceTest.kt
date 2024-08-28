package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.FirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.HdcLicence

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
    assertThat(result?.curfewHours).isEqualTo(
      CurfewHours(
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
      ),
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
    assertThat(result?.curfewHours).isEqualTo(
      CurfewHours(
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
      ),
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
    assertThat(result?.curfewHours).isEqualTo(
      CurfewHours(
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
      ),
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
    assertThat(result?.curfewHours).isEqualTo(
      CurfewHours(
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
        "19:00", "07:00",
      ),
    )
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
