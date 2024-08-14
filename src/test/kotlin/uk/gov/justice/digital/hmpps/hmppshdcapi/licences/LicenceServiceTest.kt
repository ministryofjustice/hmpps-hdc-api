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
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner

class LicenceServiceTest {
  private val licenceRepository = mock<LicenceRepository>()
  private val prisonApiClient = mock<PrisonApiClient>()
  private val objectMapper = ObjectMapper().apply {
    registerModule(Jdk8Module())
    registerModule(JavaTimeModule())
    registerKotlinModule()
  }

  private val service = LicenceService(licenceRepository, prisonApiClient, objectMapper)

  @BeforeEach
  fun reset() {
    reset(licenceRepository, prisonApiClient, objectMapper)
  }

  @Test
  fun `will retrieve HDC licence with a preferred address`() {
    whenever(licenceRepository.findLicenceByBookingId(1L)).thenReturn(licencewithPreferredAddress)

    val prisoner = prisoner.copy(topupSupervisionExpiryDate = today.minusDays(1), licenceExpiryDate = today)
    val result = service.isToBeSoftDeleted(prisoner)

    assertThat(result).isTrue
  }

  data class TestLicence(
    val bookingId: Long,
    val licenceData: LicenceData,
  )

  private companion object {
    val prisoner = Prisoner(
      prisonerNumber = "A1234BC",
      bookingId = "1L",
      prisonId = "MDI",
      topupSupervisionExpiryDate = null,
      licenceExpiryDate = null,
    )

    val newPreferredAddressLicence = { bookingId: Long, address: Address ->
      TestLicence(
        bookingId = bookingId,
        LicenceData(
          bassReferral = Cas2Referral(
            bassRequest = Cas2Request(
              bassRequested = "No",
            ),
          ),
          proposedAddress = ProposedAddress(
            curfewAddress = address,
          ),
        ),
      )
    }

    val newCas2Licence = { bookingId: Long, address: Address ->
      TestLicence(
        bookingId = bookingId,
        LicenceData(
          bassReferral = Cas2Referral(
            bassOffer = address,
            bassRequest = Cas2Request(
              bassRequested = "Yes",
            ),
          ),
          proposedAddress = ProposedAddress(),
        ),
      )
    }

    val preferredAddress = Address(
      addressLine1 = "1 The Street",
      addressLine2 = "Something Close",
      addressTown = "Town",
      postCode = "AB1 2CD",
    )

    val cas2Address = Address(
      addressLine1 = "2 Road",
      addressLine2 = "Something Else Avenue",
      addressTown = "Town",
      postCode = "EF3 4HI",
    )
    val licencewithPreferredAddress = newPreferredAddressLicence(1L, preferredAddress)

    val licencewithCas2Address = newCas2Licence(2L, cas2Address)
  }
}
