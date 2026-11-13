package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.aPreferredAddressLicence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.TestData.prisoner
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import java.time.LocalDate

class HdcStatusServiceTest {
  private val prisonSearchApiClient = mock<PrisonSearchApiClient>()
  private val service = HdcStatusService(prisonSearchApiClient)
  private val hdced = LocalDate.of(2026, 1, 1)
  private val licenceData = requireNotNull(aPreferredAddressLicence().licence).copy(approval = null)

  @Test
  fun `should return APPROVED when HDC release is approved`() {
    // Given
    val data = licenceData.copy(approval = releaseDecision(Decision.YES))

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_CA)

    // Then
    assertThat(result).isEqualTo(HdcStatus.APPROVED)
  }

  @Test
  fun `should return NOT_A_HDC_RELEASE when hdced is null regardless of approval`() {
    // Given
    val data = licenceData.copy(approval = releaseDecision(Decision.YES))

    // When
    val result = service.determineHdcStatus(null, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should return NOT_A_HDC_RELEASE when release is rejected`() {
    // Given
    val data = licenceData.copy(approval = releaseDecision(Decision.NO))

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should return NOT_A_HDC_RELEASE when final checks refuse release`() {
    // Given
    val data = licenceData.copy(
      finalChecks = FinalChecks(refusal = Refusal(Decision.YES, reason = "addressUnsuitable")),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_CA)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should return NOT_A_HDC_RELEASE when opted out`() {
    // Given
    val data = licenceData.copy(
      proposedAddress = ProposedAddress(optOut = DecisionMade(Decision.YES)),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should use stage based status when opted back in`() {
    // Given
    val data = licenceData.copy(
      proposedAddress = ProposedAddress(optOut = DecisionMade(Decision.NO)),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)
  }

  @Test
  fun `non release decisions should take precedence over approval`() {
    // Given
    val data = licenceData.copy(
      approval = releaseDecision(Decision.YES),
      finalChecks = FinalChecks(refusal = Refusal(Decision.YES, reason = "addressUnsuitable")),
      proposedAddress = ProposedAddress(optOut = DecisionMade(Decision.YES)),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should use stage based status when refusal is no or undecided`() {
    // Given
    val data = licenceData.copy(
      approval = releaseDecision(null),
      finalChecks = FinalChecks(refusal = Refusal(Decision.NO, reason = null)),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_CA)

    // Then
    assertThat(result).isEqualTo(HdcStatus.RISK_CHECKS_COMPLETE)
  }

  @Test
  fun `should preserve stage based status for postponed cases`() {
    // Given
    val data = licenceData.copy(
      finalChecks = FinalChecks(postpone = Postpone("1", Decision.YES, "awaitingInformation")),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_CA)

    // Then
    assertThat(result).isEqualTo(HdcStatus.RISK_CHECKS_COMPLETE)
  }

  @ParameterizedTest
  @CsvSource(
    "Yes, NOT_A_HDC_RELEASE",
    "No, ELIGIBILITY_CHECKS_COMPLETE",
    ", ELIGIBILITY_CHECKS_COMPLETE",
  )
  fun `should handle exclusion decision`(decision: String?, expected: HdcStatus) {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(excluded = Excluded(decision.toDecision())),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(
    "Yes, Yes, ELIGIBILITY_CHECKS_COMPLETE",
    "Yes, No, NOT_A_HDC_RELEASE",
    "Yes, , ELIGIBILITY_CHECKS_COMPLETE",
    "No, Yes, ELIGIBILITY_CHECKS_COMPLETE",
    "No, No, ELIGIBILITY_CHECKS_COMPLETE",
    "No, , ELIGIBILITY_CHECKS_COMPLETE",
    ", Yes, ELIGIBILITY_CHECKS_COMPLETE",
    ", No, ELIGIBILITY_CHECKS_COMPLETE",
    ", , ELIGIBILITY_CHECKS_COMPLETE",
  )
  fun `should handle insufficient time and decision maker approval`(
    decision: String?,
    dmApproval: String?,
    expected: HdcStatus,
  ) {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(
        crdTime = CrdTime(decision.toDecision(), dmApproval.toDecision()),
      ),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(
    "Yes, Yes, ELIGIBILITY_CHECKS_COMPLETE",
    "Yes, No, NOT_A_HDC_RELEASE",
    "Yes, , ELIGIBILITY_CHECKS_COMPLETE",
    "No, Yes, ELIGIBILITY_CHECKS_COMPLETE",
    "No, No, ELIGIBILITY_CHECKS_COMPLETE",
    "No, , ELIGIBILITY_CHECKS_COMPLETE",
    ", Yes, ELIGIBILITY_CHECKS_COMPLETE",
    ", No, ELIGIBILITY_CHECKS_COMPLETE",
    ", , ELIGIBILITY_CHECKS_COMPLETE",
  )
  fun `should handle presumptive unsuitability and exceptional circumstances`(
    decision: String?,
    exceptionalCircumstances: String?,
    expected: HdcStatus,
  ) {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(
        suitability = Suitability(decision.toDecision()),
        exceptionalCircumstances = DecisionMade(exceptionalCircumstances.toDecision()),
      ),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `should use stage based status when exceptional circumstances section is missing`() {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(suitability = Suitability(Decision.YES)),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)
  }

  @Test
  fun `exception approvals should not override exclusion`() {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(
        excluded = Excluded(Decision.YES),
        suitability = Suitability(Decision.YES),
        exceptionalCircumstances = DecisionMade(Decision.YES),
        crdTime = CrdTime(Decision.YES, dmApproval = Decision.YES),
      ),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.ELIGIBILITY)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should allow eligibility checks to progress with both exception approvals`() {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(
        excluded = Excluded(Decision.NO),
        suitability = Suitability(Decision.YES),
        exceptionalCircumstances = DecisionMade(Decision.YES),
        crdTime = CrdTime(Decision.YES, dmApproval = Decision.YES),
      ),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.ELIGIBILITY_CHECKS_COMPLETE)
  }

  @Test
  fun `eligibility non release decisions should take precedence over approval`() {
    // Given
    val data = licenceData.copy(
      approval = releaseDecision(Decision.YES),
      eligibility = Eligibility(
        excluded = Excluded(Decision.YES),
        suitability = Suitability(Decision.YES),
        crdTime = CrdTime(Decision.YES),
      ),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `dm approval should not override non release decision`() {
    // Given
    val data = licenceData.copy(
      eligibility = Eligibility(
        crdTime = CrdTime(Decision.YES, dmApproval = Decision.NO),
      ),
    )

    // When
    val result = service.determineHdcStatus(hdced, data, HdcStage.PROCESSING_RO)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_A_HDC_RELEASE)
  }

  @Test
  fun `should return NOT_STARTED when stage is null`() {
    // Given
    val licence = null

    // When
    val result = service.determineHdcStatus(hdced, licence, null)

    // Then
    assertThat(result).isEqualTo(HdcStatus.NOT_STARTED)
  }

  @ParameterizedTest
  @CsvSource(
    "ELIGIBILITY, NOT_STARTED",
    "PROCESSING_RO, ELIGIBILITY_CHECKS_COMPLETE",
    "PROCESSING_CA, RISK_CHECKS_COMPLETE",
    "APPROVAL, RISK_CHECKS_COMPLETE",
    "MODIFIED, RISK_CHECKS_COMPLETE",
  )
  fun `should use stage based status without an approval decision`(
    stage: HdcStage,
    status: HdcStatus,
  ) {
    // Given
    val data = licenceData

    // When
    val resultWithLicence = service.determineHdcStatus(hdced, data, stage)
    val resultWithoutLicence = service.determineHdcStatus(hdced, null, stage)

    // Then
    assertThat(resultWithLicence).isEqualTo(status)
    assertThat(resultWithoutLicence).isEqualTo(status)
  }

  @Test
  fun `should calculate bulk statuses using supplied licences and a single prisoner search`() {
    // Given
    val bookingIds = listOf(1L, 2L, 3L, 4L, 5L, 6L)

    whenever(prisonSearchApiClient.getPrisonersByBookingIds(bookingIds)).thenReturn(
      bookingIds.dropLast(1).reversed().map {
        prisoner().copy(
          bookingId = it.toString(),
          homeDetentionCurfewEligibilityDate = hdced,
        )
      },
    )

    val licences = listOf(
      licence(2L, licenceData.copy(proposedAddress = ProposedAddress(optOut = DecisionMade(Decision.YES)))),
      licence(1L, licenceData.copy(approval = releaseDecision(Decision.YES)), HdcStage.PROCESSING_CA),
      licence(3L, licenceData.copy(proposedAddress = ProposedAddress(optOut = DecisionMade(Decision.NO)))),
      licence(5L, null),
      licence(6L, licenceData.copy(approval = releaseDecision(Decision.YES)), HdcStage.PROCESSING_CA),
    )

    // When
    val result = service.getForBookingIds(bookingIds, licences)

    // Then
    assertThat(result.map { it.bookingId }).containsExactlyElementsOf(bookingIds)
    assertThat(result.map { it.status }).containsExactly(
      HdcStatus.APPROVED,
      HdcStatus.NOT_A_HDC_RELEASE,
      HdcStatus.ELIGIBILITY_CHECKS_COMPLETE,
      HdcStatus.NOT_STARTED,
      HdcStatus.ELIGIBILITY_CHECKS_COMPLETE,
      HdcStatus.NOT_A_HDC_RELEASE,
    )

    verify(prisonSearchApiClient).getPrisonersByBookingIds(bookingIds)
    verifyNoMoreInteractions(prisonSearchApiClient)
  }

  @Test
  fun `should calculate single booking status from supplied licence`() {
    // Given
    whenever(prisonSearchApiClient.getPrisonersByBookingIds(listOf(1L))).thenReturn(
      listOf(
        prisoner().copy(
          bookingId = "1",
          homeDetentionCurfewEligibilityDate = hdced,
        ),
      ),
    )

    val licence = licence(
      1L,
      licenceData.copy(approval = releaseDecision(Decision.YES)),
      HdcStage.PROCESSING_CA,
    )

    // When
    val result = service.getForBooking(1L, licence)

    // Then
    assertThat(result).isEqualTo(HdcStatus.APPROVED)
  }

  private fun String?.toDecision(): Decision? = this?.let { Decision.valueOf(it.uppercase()) }

  private fun releaseDecision(decision: Decision?) = Approval(release = Release(decision, decisionMaker = null, reasonForDecision = null))

  private fun licence(bookingId: Long, data: LicenceData?, licenceStage: HdcStage = HdcStage.PROCESSING_RO): Licence = mock {
    on { this.bookingId }.thenReturn(bookingId)
    on { stage }.thenReturn(licenceStage)
    on { licence }.thenReturn(data)
  }
}
