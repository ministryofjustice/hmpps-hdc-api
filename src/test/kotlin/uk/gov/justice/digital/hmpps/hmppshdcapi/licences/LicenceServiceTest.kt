package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HmppsHdcApiExceptionHandler.NoDataFoundException
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.AddressType
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewAddress as ModelCurfewAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.CurfewTimes as ModelCurfewTimes
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.FirstNight as ModelFirstNight

class LicenceServiceTest {
  private val licenceRepository = mock<LicenceRepository>()
  private val hdcStatusService = mock<HdcStatusService>()

  private val service = LicenceService(
    licenceRepository = licenceRepository,
    hdcStatusService = hdcStatusService,
  )

  @BeforeEach
  fun reset() {
    reset(licenceRepository)
  }

  @Test
  fun `will retrieve HDC licence with an approved preferred address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCurfewApprovedPremisesRequiredLicence()))
    whenever(hdcStatusService.getForBooking(54321L, TestData.aCurfewApprovedPremisesRequiredLicence())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      ModelCurfewAddress(
        "4 The Street",
        "Area 4",
        "Town 4",
        null,
        "TS4 4TS",
        AddressType.CAS,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      ModelFirstNight(
        LocalTime.of(16, 0),
        LocalTime.of(8, 0),
      ),
    )
    assertThat(result?.curfewTimes).isEqualTo(
      listOf(
        ModelCurfewTimes(
          DayOfWeek.MONDAY,
          LocalTime.of(20, 0),
          DayOfWeek.TUESDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.TUESDAY,
          LocalTime.of(20, 0),
          DayOfWeek.WEDNESDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.WEDNESDAY,
          LocalTime.of(20, 0),
          DayOfWeek.THURSDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.THURSDAY,
          LocalTime.of(20, 0),
          DayOfWeek.FRIDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.FRIDAY,
          LocalTime.of(20, 0),
          DayOfWeek.SATURDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.SATURDAY,
          LocalTime.of(20, 0),
          DayOfWeek.SUNDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
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
    whenever(hdcStatusService.getForBooking(54321L, TestData.aCas2ApprovedPremisesLicence())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      ModelCurfewAddress(
        "3 The Avenue",
        "Area 3",
        "Town 3",
        null,
        "TS5 5TS",
        AddressType.CAS,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      ModelFirstNight(
        LocalTime.of(15, 0),
        LocalTime.of(7, 0),
      ),
    )

    assertThat(result?.curfewTimes).isEqualTo(
      listOf(
        ModelCurfewTimes(
          DayOfWeek.MONDAY,
          LocalTime.of(19, 0),
          DayOfWeek.TUESDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.TUESDAY,
          LocalTime.of(19, 0),
          DayOfWeek.WEDNESDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.WEDNESDAY,
          LocalTime.of(19, 0),
          DayOfWeek.THURSDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.THURSDAY,
          LocalTime.of(19, 0),
          DayOfWeek.FRIDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.FRIDAY,
          LocalTime.of(19, 0),
          DayOfWeek.SATURDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.SATURDAY,
          LocalTime.of(19, 0),
          DayOfWeek.SUNDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.SUNDAY,
          LocalTime.of(19, 0),
          DayOfWeek.MONDAY,
          LocalTime.of(7, 0),
        ),
      ),
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will retrieve HDC licence with a Cas2 address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCas2Licence()))
    whenever(hdcStatusService.getForBooking(54321L, TestData.aCas2Licence())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull
    assertThat(result?.curfewAddress).isEqualTo(
      ModelCurfewAddress(
        "2 The Street",
        "Area 2",
        "Town 2",
        null,
        "TS6 6TS",
        AddressType.CAS,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      ModelFirstNight(
        LocalTime.of(15, 0),
        LocalTime.of(7, 0),
      ),
    )

    assertThat(result?.curfewTimes).isEqualTo(
      listOf(
        ModelCurfewTimes(
          DayOfWeek.MONDAY,
          LocalTime.of(19, 0),
          DayOfWeek.TUESDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.TUESDAY,
          LocalTime.of(19, 0),
          DayOfWeek.WEDNESDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.WEDNESDAY,
          LocalTime.of(19, 0),
          DayOfWeek.THURSDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.THURSDAY,
          LocalTime.of(19, 0),
          DayOfWeek.FRIDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.FRIDAY,
          LocalTime.of(19, 0),
          DayOfWeek.SATURDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.SATURDAY,
          LocalTime.of(19, 0),
          DayOfWeek.SUNDAY,
          LocalTime.of(7, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.SUNDAY,
          LocalTime.of(19, 0),
          DayOfWeek.MONDAY,
          LocalTime.of(7, 0),
        ),
      ),
    )

    assertThat(result?.status).isEqualTo(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will retrieve HDC licence with a preferred address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aPreferredAddressLicence()))
    whenever(hdcStatusService.getForBooking(54321L, TestData.aPreferredAddressLicence())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    val result = service.getByBookingId(54321L)

    assertThat(result).isNotNull

    assertThat(result?.curfewAddress).isEqualTo(
      ModelCurfewAddress(
        "1 The Street",
        "Area",
        "Town",
        null,
        "TS7 7TS",
        AddressType.RESIDENTIAL,
      ),
    )
    assertThat(result?.firstNightCurfewHours).isEqualTo(
      ModelFirstNight(
        LocalTime.of(16, 0),
        LocalTime.of(8, 0),
      ),
    )

    assertThat(result?.curfewTimes).isEqualTo(
      listOf(
        ModelCurfewTimes(
          DayOfWeek.MONDAY,
          LocalTime.of(20, 0),
          DayOfWeek.TUESDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.TUESDAY,
          LocalTime.of(20, 0),
          DayOfWeek.WEDNESDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.WEDNESDAY,
          LocalTime.of(20, 0),
          DayOfWeek.THURSDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.THURSDAY,
          LocalTime.of(20, 0),
          DayOfWeek.FRIDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.FRIDAY,
          LocalTime.of(20, 0),
          DayOfWeek.SATURDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
          DayOfWeek.SATURDAY,
          LocalTime.of(20, 0),
          DayOfWeek.SUNDAY,
          LocalTime.of(8, 0),
        ),
        ModelCurfewTimes(
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
  fun `will correctly format a short Cas2 address`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aCas2LicenceWithShortAddress()))
    whenever(hdcStatusService.getForBooking(54321L, TestData.aCas2LicenceWithShortAddress())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    val result = service.getByBookingId(54321L)

    assertThat(result?.curfewAddress).isEqualTo(
      ModelCurfewAddress(
        "2 The Street",
        null,
        "Town 2",
        null,
        "TS6 6TS",
        AddressType.CAS,
      ),
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will return null for curfewTimes when a single curfew time is null`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aLicenceWithSingleMissingCurfewHour()))
    whenever(hdcStatusService.getForBooking(54321L, TestData.aLicenceWithSingleMissingCurfewHour())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)

    val result = service.getByBookingId(54321L)

    assertThat(result?.curfewTimes).isEqualTo(
      null,
    )

    verify(licenceRepository, times(1)).findByBookingIds(listOf(54321L))
  }

  @Test
  fun `will return null for curfewTimes when multiple curfew times are null`() {
    whenever(licenceRepository.findByBookingIds(listOf(54321L))).thenReturn(listOf(TestData.aLicenceWithMultipleMissingCurfewHours()))
    whenever(hdcStatusService.getForBooking(54321L, TestData.aLicenceWithMultipleMissingCurfewHours())).thenReturn(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)
    val result = service.getByBookingId(54321L)

    assertThat(result?.curfewTimes).isEqualTo(
      null,
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

  @Nested
  inner class GetAddress {
    @Test
    fun `test getAddress when curfew approved premise is required`() {
      val result = service.getAddress(aCurfew, aCas2Referral, aProposedAddress, 1L)!!

      with(aCurfew.approvedPremisesAddress!!) {
        assertThat(result.addressLine1).isEqualTo(addressLine1)
        assertThat(result.addressLine2).isEqualTo(addressLine2)
        assertThat(result.townOrCity).isEqualTo(addressTown)
        assertThat(result.postcode).isEqualTo(postCode)
      }
    }

    @Test
    fun `test getAddress when cas2 approved premise is required`() {
      val anApprovedCas2Referral = aCas2Referral.copy(bassAreaCheck = Cas2AreaCheck(Decision.YES))
      val result = service.getAddress(aCurfew, anApprovedCas2Referral, aProposedAddress, 1L)!!

      with(aCas2Referral.approvedPremisesAddress!!) {
        assertThat(result.addressLine1).isEqualTo(addressLine1)
        assertThat(result.addressLine2).isEqualTo(addressLine2)
        assertThat(result.townOrCity).isEqualTo(addressTown)
        assertThat(result.postcode).isEqualTo(postCode)
      }
    }

    @Test
    fun `test getAddress when cas2 address is required`() {
      val noCurfewApprovedPremisesRequired = aCurfew.copy(
        approvedPremises = ApprovedPremises(
          Decision.NO,
        ),
      )

      val result = service.getAddress(noCurfewApprovedPremisesRequired, aCas2Referral, aProposedAddress, 1L)!!

      with(aCas2Referral.bassOffer!!) {
        assertThat(result.addressLine1).isEqualTo(addressLine1)
        assertThat(result.addressLine2).isEqualTo(addressLine2)
        assertThat(result.townOrCity).isEqualTo(addressTown)
        assertThat(result.postcode).isEqualTo(postCode)
      }
    }

    @Test
    fun `test getAddress when no curfew or Cas2 address is required`() {
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

      val result = service.getAddress(noCurfewApprovedPremisesRequired, noCas2Referral, aProposedAddress, 1L)!!

      with(aProposedAddress.curfewAddress!!) {
        assertThat(result.addressLine1).isEqualTo(addressLine1)
        assertThat(result.addressLine2).isEqualTo(addressLine2)
        assertThat(result.townOrCity).isEqualTo(addressTown)
        assertThat(result.postcode).isEqualTo(postCode)
      }
    }

    @Test
    fun `address object is successfully created when address line 2 is not present`() {
      val curfewAddress = CurfewAddress(
        "5 The Street",
        null,
        "Town 5",
        "TS8 8TS",
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

      val result = service.getAddress(noCurfewApprovedPremisesRequired, noCas2Referral, anotherProposedAddress, 1L)!!

      with(curfewAddress) {
        assertThat(result.addressLine1).isEqualTo(addressLine1)
        assertThat(result.addressLine2).isEqualTo(addressLine2)
        assertThat(result.townOrCity).isEqualTo(addressTown)
        assertThat(result.postcode).isEqualTo(postCode)
      }
    }

    @Test
    fun `test getAddress will return null when the curfew address is null`() {
      val aCurfewWithoutAnAddress = aCurfew.copy(
        approvedPremisesAddress = null,
      )

      val result = service.getAddress(aCurfewWithoutAnAddress, aCas2Referral, aProposedAddress, 1L)

      assertThat(result).isNull()
    }

    @Test
    fun `test getAddress will return null when a single address field is null`() {
      val aCurfewWithAMissingAddressLine = aCurfew.copy(
        approvedPremisesAddress = AddressAndPhone(
          addressLine1 = null,
          addressTown = "Town 1",
          postCode = "TS7 7TS",
        ),
      )

      val result = service.getAddress(aCurfewWithAMissingAddressLine, aCas2Referral, aProposedAddress, 1L)

      assertThat(result).isNull()
    }

    @Test
    fun `test getAddress will return null when multiple address fields are blank`() {
      val aCurfewWithMultipleMissingAddressLines = aCurfew.copy(
        approvedPremisesAddress = AddressAndPhone(
          addressLine1 = "",
          addressLine2 = null,
          addressTown = "Town 1",
          postCode = "",
        ),
      )

      val result = service.getAddress(aCurfewWithMultipleMissingAddressLines, aCas2Referral, aProposedAddress, 1L)

      assertThat(result).isNull()
    }
  }

  private companion object {

    val aCurfew = Curfew(
      firstNight = FirstNight(
        LocalTime.of(16, 0),
        LocalTime.of(8, 0),
      ),
      curfewHours = CurfewHours(
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        LocalTime.of(8, 0),
        null,
        null,
      ),
      approvedPremisesAddress = AddressAndPhone(
        "2 The Street",
        "Area 2",
        "Town 2",
        "TS9 9TS",
      ),
      approvedPremises = ApprovedPremises(
        Decision.YES,
      ),
    )

    val aCas2Offer = Cas2Offer(
      "3 The Street",
      "Area 3",
      "Town 3",
      "TS91 0TS",
      OfferAccepted.YES,
    )

    val aCas2Referral = CurrentCas2Referral(
      aCas2Offer,
      Cas2Request(
        Decision.YES,
      ),
      AddressAndPhone(
        "4 The Street",
        "Area 4",
        "Town 4",
        "TS92 0TS",
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
        "TS8 8TS",
      ),
      addressProposed = DecisionMade(Decision.YES),
    )

    fun anExceptionLicence() = Licence(
      id = 1,
      prisonNumber = "A12345B",
      bookingId = 54321,
      stage = HdcStage.MODIFIED,
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
