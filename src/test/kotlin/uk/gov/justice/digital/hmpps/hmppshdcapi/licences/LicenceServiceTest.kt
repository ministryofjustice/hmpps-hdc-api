package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class LicenceServiceTest {
  private val licenceRepository = mock<LicenceRepository>()

  private val service = LicenceService(
    licenceRepository = licenceRepository,
  )

  @BeforeEach
  fun reset() {
    reset(licenceRepository)
  }

  @Test
  fun `will retrieve HDC licence with an approved preferred address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCurfewApprovedPremisesRequiredLicence()))

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "4 The Street",
        "Area 4",
        "Town 4",
        "MN4 5OP",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "16:00",
        "08:00",
      ),
    )
    assertThat(result?.curfewTimes).isEqualTo(
      listOf(
        CurfewTimes(
          DayOfWeek.MONDAY,
          LocalTime.of(20, 0),
          DayOfWeek.TUESDAY,
          LocalTime.of(8, 0),
        ),
        CurfewTimes(
          DayOfWeek.TUESDAY,
          LocalTime.of(20, 0),
          DayOfWeek.WEDNESDAY,
          LocalTime.of(8, 0),
        ),
        CurfewTimes(
          DayOfWeek.WEDNESDAY,
          LocalTime.of(20, 0),
          DayOfWeek.THURSDAY,
          LocalTime.of(8, 0),
        ),
        CurfewTimes(
          DayOfWeek.THURSDAY,
          LocalTime.of(20, 0),
          DayOfWeek.FRIDAY,
          LocalTime.of(8, 0),
        ),
        CurfewTimes(
          DayOfWeek.FRIDAY,
          LocalTime.of(20, 0),
          DayOfWeek.SATURDAY,
          LocalTime.of(8, 0),
        ),
        CurfewTimes(
          DayOfWeek.SATURDAY,
          LocalTime.of(20, 0),
          DayOfWeek.SUNDAY,
          LocalTime.of(8, 0),
        ),
        CurfewTimes(
          DayOfWeek.SUNDAY,
          LocalTime.of(20, 0),
          DayOfWeek.MONDAY,
          LocalTime.of(8, 0),
        ),
      ),
    )
    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will retrieve HDC licence with an approved Cas2 address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCas2ApprovedPremisesLicence()))

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "3 The Avenue",
        "Area 3",
        "Town 3",
        "IJ3 4KL",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "15:00",
        "07:00",
      ),
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will retrieve HDC licence with a Cas2 address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCas2Licence()))

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "2 The Street",
        "Area 2",
        "Town 2",
        "EF3 4GH",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "15:00",
        "07:00",
      ),
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will retrieve HDC licence with a preferred address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aPreferredAddressLicence()))

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "1 The Street",
        "Area",
        "Town",
        "AB1 2CD",
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      FirstNight(
        "16:00",
        "08:00",
      ),
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will correctly format a short Cas2 address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCas2LicenceWithShortAddress()))

    val result = service.getByBookingId(54321L)

    assertThat(result?.curfewAddress).isEqualTo(
      CurfewAddress(
        "2 The Street",
        null,
        "Town 2",
        "EF3 4GH",
      ),
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will throw exception if no HDC licence found`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(emptyList())

    val exception = assertThrows<NoDataFoundException> {
      service.getByBookingId(54321L)
    }

    assertThat(exception).isInstanceOf(NoDataFoundException::class.java)
    assertThat(exception.message).isEqualTo("No licence found for booking id 54321")

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will throw exception if no HDC licence data found`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(anExceptionLicence()))

    val exception = assertThrows<NoDataFoundException> {
      service.getByBookingId(54321L)
    }

    assertThat(exception).isInstanceOf(NoDataFoundException::class.java)
    assertThat(exception.message).isEqualTo("No licence data found for booking id 54321")

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `test getLicence when curfew approved premise is required`() {
    val result = service.getAddress(aCurfew, aCas2Referral, aProposedAddress)!!

    with(aCurfew.approvedPremisesAddress!!) {
      assertThat(result.addressLine1).isEqualTo(addressLine1)
      assertThat(result.addressLine2).isEqualTo(addressLine2)
      assertThat(result.addressTown).isEqualTo(addressTown)
      assertThat(result.postCode).isEqualTo(postCode)
    }
  }

  @Test
  fun `test getLicence when cas2 approved premise is required`() {
    val anApprovedCas2Referral = aCas2Referral.copy(bassAreaCheck = Cas2AreaCheck(Decision.YES))
    val result = service.getAddress(aCurfew, anApprovedCas2Referral, aProposedAddress)!!

    with(aCas2Referral.approvedPremisesAddress!!) {
      assertThat(result.addressLine1).isEqualTo(addressLine1)
      assertThat(result.addressLine2).isEqualTo(addressLine2)
      assertThat(result.addressTown).isEqualTo(addressTown)
      assertThat(result.postCode).isEqualTo(postCode)
    }
  }

  @Test
  fun `test getLicence when cas2 address is required`() {
    val noCurfewApprovedPremisesRequired = aCurfew.copy(
      approvedPremises = ApprovedPremises(
        Decision.NO,
      ),
    )

    val result = service.getAddress(noCurfewApprovedPremisesRequired, aCas2Referral, aProposedAddress)!!

    with(aCas2Referral.bassOffer!!) {
      assertThat(result.addressLine1).isEqualTo(addressLine1)
      assertThat(result.addressLine2).isEqualTo(addressLine2)
      assertThat(result.addressTown).isEqualTo(addressTown)
      assertThat(result.postCode).isEqualTo(postCode)
    }
  }

  @Test
  fun `test getLicence when no curfew or Cas2 address is required`() {
    val noCurfewApprovedPremisesRequired = aCurfew.copy(
      approvedPremises = ApprovedPremises(
        Decision.NO,
      ),
    )
    val noCas2Referral = aCas2Referral.copy(
      bassOffer = aCas2Offer.copy(
        bassAccepted = OfferAccepted.UNSUITABLE,
      ),
      bassRequest = Cas2Request(
        Decision.NO,
      ),
    )

    val result = service.getAddress(noCurfewApprovedPremisesRequired, noCas2Referral, aProposedAddress)

    assertThat(result).isEqualTo(aProposedAddress.curfewAddress)
  }

  @Test
  fun `address object is successfully created when address line 2 is not present`() {
    val curfewAddress = CurfewAddress(
      "5 The Street",
      null,
      "Town 5",
      "KL5 5MN",
    )

    val noCurfewApprovedPremisesRequired = aCurfew.copy(
      approvedPremises = ApprovedPremises(
        Decision.NO,
      ),
    )
    val noCas2Referral = aCas2Referral.copy(
      bassOffer = aCas2Offer.copy(
        bassAccepted = OfferAccepted.UNAVAILABLE,
      ),
      bassRequest = Cas2Request(
        Decision.NO,
      ),
    )

    val anotherProposedAddress = aProposedAddress.copy(
      curfewAddress,
    )

    val result = service.getAddress(noCurfewApprovedPremisesRequired, noCas2Referral, anotherProposedAddress)

    assertThat(result).isEqualTo(curfewAddress)
  }

  private companion object {

    val aCurfew = Curfew(
      FirstNight(
        "16:00",
        "08:00",
      ),
      CurfewHours(
        "20:00",
        "08:00",
        "20:00",
        "08:00",
        "20:00",
        "08:00",
        "20:00",
        "08:00",
        "20:00",
        "08:00",
        "20:00",
        "08:00",
        "20:00",
        "08:00",
      ),
      CurfewAddress(
        "2 The Street",
        "Area 2",
        "Town 2",
        "EF1 2GH",
      ),
      ApprovedPremises(
        Decision.YES,
      ),
    )

    val aCas2Offer = Cas2Offer(
      "3 The Street",
      "Area 3",
      "Town 3",
      "GH3 3IJ",
      OfferAccepted.YES,
    )

    val aCas2Referral = Cas2Referral(
      aCas2Offer,
      Cas2Request(
        Decision.YES,
      ),
      CurfewAddress(
        "4 The Street",
        "Area 4",
        "Town 4",
        "IJ4 4KL",
      ),
      Cas2AreaCheck(
        Decision.NO,
      ),
    )

    val aProposedAddress = ProposedAddress(
      CurfewAddress(
        "5 The Street",
        "Area 5",
        "Town 5",
        "KL5 5MN",
      ),
    )

    fun anExceptionLicence() = Licence(
      id = 1,
      prisonNumber = "A12345B",
      bookingId = 54321,
      stage = "MODIFIED",
      version = 1,
      transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
      varyVersion = 0,
      additionalConditionsVersion = null,
      standardConditionsVersion = null,
      deletedAt = null,
      licenceInCvl = false,
      licence = null,
    )
  }
}
