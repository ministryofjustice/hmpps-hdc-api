package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.aPreferredAddressLicence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.hdcPrisonerStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import java.time.LocalDate

class HdcStatusServiceTest {
  private val prisonSearchApiClient = mock<PrisonSearchApiClient>()
  private val prisonApiClient = mock<PrisonApiClient>()
  private val service = HdcStatusService(
    prisonSearchApiClient = prisonSearchApiClient,
    prisonApiClient = prisonApiClient,
  )

  @Test
  fun `should return APPROVED when nomis approved and hdced present`() {
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(listOf(prisoner().copy(bookingId = "1", homeDetentionCurfewEligibilityDate = LocalDate.now())))
    whenever(prisonApiClient.getHdcStatus(1L)).thenReturn(
      hdcPrisonerStatus().copy(bookingId = 1L, approvalStatus = "APPROVED"),
    )
    val licence = aPreferredAddressLicence(bookingId = 1L, stage = HdcStage.ELIGIBILITY)
    val result = service.getForBooking(1L, licence)
    assertThat(HdcStatus.APPROVED).isEqualTo(result)
  }

  @ParameterizedTest
  @CsvSource(
    "APPROVED",
    "REJECTED",
    "INELIGIBLE",
    "ANYTHING",
  )
  fun `should return NOT_A_HDC_RELEASE when hdced is null`(approvalStatus: String) {
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(listOf(prisoner().copy(bookingId = "1", homeDetentionCurfewEligibilityDate = null)))
    whenever(prisonApiClient.getHdcStatus(1L)).thenReturn(
      hdcPrisonerStatus().copy(bookingId = 1L, approvalStatus = approvalStatus),
    )
    val licence = aPreferredAddressLicence(bookingId = 1L, stage = HdcStage.PROCESSING_RO)
    val result = service.getForBooking(1L, licence)
    assertThat(HdcStatus.NOT_A_HDC_RELEASE).isEqualTo(result)
  }

  @ParameterizedTest
  @CsvSource(
    "REJECTED",
    "OPT_OUT ACCO",
    "OPT_OUT OTH",
    "INELIGIBLE",
    "PRES UNSUIT",
  )
  fun `should return NOT_A_HDC_RELEASE for explicit non release statuses`(approvalStatus: String) {
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(listOf(prisoner().copy(bookingId = "1", homeDetentionCurfewEligibilityDate = LocalDate.now())))
    whenever(prisonApiClient.getHdcStatus(1L)).thenReturn(
      hdcPrisonerStatus().copy(bookingId = 1L, approvalStatus = approvalStatus),
    )
    val licence = aPreferredAddressLicence(bookingId = 1L, stage = HdcStage.PROCESSING_RO)
    val result = service.getForBooking(1L, licence)
    assertThat(HdcStatus.NOT_A_HDC_RELEASE).isEqualTo(result)
  }

  @Test
  fun `should return NOT_STARTED when stage is null or eligibility`() {
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(listOf(prisoner().copy(bookingId = "1", homeDetentionCurfewEligibilityDate = LocalDate.now())))
    whenever(prisonApiClient.getHdcStatus(1L)).thenReturn(
      hdcPrisonerStatus().copy(bookingId = 1L, approvalStatus = "PP INVEST"),
    )
    val licence = aPreferredAddressLicence(bookingId = 1L, stage = HdcStage.ELIGIBILITY)
    val result = service.getForBooking(1L, licence)
    assertThat(HdcStatus.NOT_STARTED).isEqualTo(result)
  }

  @Test
  fun `should return ELIGIBILITY_CHECKS_COMPLETE when processing ro`() {
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(listOf(prisoner().copy(bookingId = "1", homeDetentionCurfewEligibilityDate = LocalDate.now())))
    whenever(prisonApiClient.getHdcStatus(1L)).thenReturn(
      hdcPrisonerStatus().copy(bookingId = 1L, approvalStatus = "ANY_OTHER_STATUS"),
    )
    val licence = aPreferredAddressLicence(bookingId = 1L, stage = HdcStage.PROCESSING_RO)
    val result = service.getForBooking(1L, licence)
    assertThat(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE).isEqualTo(result)
  }

  @ParameterizedTest
  @CsvSource(
    "PROCESSING_CA, ",
    "APPROVAL, ",
    "MODIFIED, ",
  )
  fun `should return RISK_CHECKS_COMPLETE for remaining stages`(
    hdcStageName: String,
  ) {
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(any())).thenReturn(listOf(prisoner().copy(bookingId = "1", homeDetentionCurfewEligibilityDate = LocalDate.now())))
    whenever(prisonApiClient.getHdcStatus(1L)).thenReturn(
      hdcPrisonerStatus().copy(bookingId = 1L, approvalStatus = "OTHER"),
    )
    val stage = HdcStage.valueOf(hdcStageName)
    val licence = aPreferredAddressLicence(bookingId = 1L, stage = stage)
    val result = service.getForBooking(1L, licence)
    assertThat(HdcStatus.RISK_CHECKS_COMPLETE).isEqualTo(result)
  }
}
