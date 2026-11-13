package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.BookingHdcStatus
import java.time.LocalDate

@Service
class HdcStatusService(
  private val prisonSearchApiClient: PrisonSearchApiClient,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  private fun LicenceData?.isNonRelease(): Boolean = this?.approval?.release?.decision == Decision.NO ||
    this?.finalChecks?.refusal?.decision == Decision.YES ||
    this?.proposedAddress?.optOut?.decision == Decision.YES ||
    this?.eligibility?.isNonRelease() == true

  private fun Eligibility.isNonRelease(): Boolean = excluded?.decision == Decision.YES ||
    (crdTime?.decision == Decision.YES && crdTime.dmApproval == Decision.NO) ||
    (suitability?.decision == Decision.YES && exceptionalCircumstances?.decision == Decision.NO)

  fun getForBooking(bookingId: Long, licence: Licence): HdcStatus = getForBookingIds(listOf(bookingId), listOf(licence)).first().status

  fun getForBookingIds(
    bookingIds: List<Long>,
    licences: List<Licence>,
  ): List<BookingHdcStatus> {
    val licencesByBookingId = licences.associateBy { it.bookingId }

    val prisonersById = prisonSearchApiClient
      .getPrisonersByBookingIds(bookingIds)
      .associateBy { it.bookingId.toLong() }

    return buildList {
      bookingIds.forEach { id ->
        val prisoner = prisonersById[id]
        val licence = licencesByBookingId[id]

        add(
          BookingHdcStatus(
            bookingId = id,
            status = determineHdcStatus(
              prisoner?.homeDetentionCurfewEligibilityDate,
              licence?.licence,
              licence?.stage,
            ),
          ),
        )
      }
    }
  }

  fun determineHdcStatus(
    hdced: LocalDate?,
    licenceData: LicenceData?,
    hdcStage: HdcStage?,
  ): HdcStatus {
    log.debug("Determining HDC status for hdced: {}, hdcStage: {}", hdced, hdcStage)

    return when {
      hdced == null || licenceData.isNonRelease() -> HdcStatus.NOT_A_HDC_RELEASE
      hdcStage == null || hdcStage == HdcStage.ELIGIBILITY -> HdcStatus.NOT_STARTED
      hdcStage == HdcStage.PROCESSING_RO -> HdcStatus.ELIGIBILITY_CHECKS_COMPLETE
      licenceData?.approval?.release?.decision == Decision.YES -> HdcStatus.APPROVED
      else -> HdcStatus.RISK_CHECKS_COMPLETE
    }
  }
}
