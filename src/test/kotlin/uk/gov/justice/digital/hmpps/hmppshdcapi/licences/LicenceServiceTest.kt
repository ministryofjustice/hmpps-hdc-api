package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    whenever(licenceRepository.findLicenceByBookingId(1L)).thenReturn(TestData.aPreferredAddressLicence())
    whenever(prisonApiClient.getBooking(1L)).thenReturn(TestData.aBooking())
    whenever(prisonApiClient.getPrisonContactDetails(TestData.aBooking().agencyId)).thenReturn(TestData.somePrisonInformation())

    val result = service.getByBookingId(1L)
  }
}
