package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient

class LicenceServiceTest {
  private val licenceRepository = mock<LicenceRepository>()
  private val prisonApiClient = mock<PrisonApiClient>()

  private val objectMapper = ObjectMapper().apply {
    registerModule(Jdk8Module())
    registerModule(JavaTimeModule())
    registerKotlinModule()
  }

  private val service = LicenceService(
    licenceRepository = licenceRepository,
    prisonApiClient = prisonApiClient,
    objectMapper = objectMapper,
  )

  @BeforeEach
  fun reset() {
    reset(licenceRepository, prisonApiClient)
  }

  @Test
  fun `will retrieve HDC licence with a preferred address`() {
    whenever(licenceRepository.findLicenceByBookingId(54321L)).thenReturn(TestData.aPreferredAddressLicence())
    whenever(prisonApiClient.getBooking(54321L)).thenReturn(TestData.aBooking())
    whenever(prisonApiClient.getPrisonContactDetails(TestData.aBooking().agencyId)).thenReturn(TestData.somePrisonInformation())

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.prisonTelephone).isEqualTo(TestData.somePrisonInformation().phones.first().number)
    assertThat(result?.curfewAddress).isEqualTo("1 The Street, Area, Town, AB1 2CD")
    assertThat(result?.firstNightCurfewHours).isEqualTo(FirstNight(
      "16:00",
      "08:00"
    ))
    assertThat(result?.curfewHours).isEqualTo(CurfewHours(
      "20:00", "08:00",
      "20:00","08:00",
      "20:00", "08:00",
      "20:00", "08:00",
      "20:00", "08:00",
      "20:00", "08:00",
      "20:00", "08:00",
    ))
  }

  @Test
  fun `will retrieve HDC licence with a Cas2 address`() {
    whenever(licenceRepository.findLicenceByBookingId(54321L)).thenReturn(TestData.aCas2Licence())
    whenever(prisonApiClient.getBooking(54321L)).thenReturn(TestData.aBooking())
    whenever(prisonApiClient.getPrisonContactDetails(TestData.aBooking().agencyId)).thenReturn(TestData.somePrisonInformation())

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.prisonTelephone).isEqualTo(TestData.somePrisonInformation().phones.first().number)
    assertThat(result?.curfewAddress).isEqualTo("2 The Street, Area 2, Town 2, EF3 4GH")
    assertThat(result?.firstNightCurfewHours).isEqualTo(FirstNight(
      "15:00",
      "07:00"
    ))
    assertThat(result?.curfewHours).isEqualTo(CurfewHours(
      "19:00", "07:00",
      "19:00","07:00",
      "19:00", "07:00",
      "19:00", "07:00",
      "19:00", "07:00",
      "19:00", "07:00",
      "19:00", "07:00",
    ))
  }
}
