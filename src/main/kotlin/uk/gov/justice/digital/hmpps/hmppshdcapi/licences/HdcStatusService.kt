package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonerHdcStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.BookingHdcStatus
import java.time.LocalDate

@Service
class HdcStatusService(
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val prisonApiClient: PrisonApiClient,
) {
  private val nonReleaseStatuses: Set<String> = setOf(
    "REJECTED",
    "INELIGIBLE",
    "PRES UNSUIT",
  ).mapTo(mutableSetOf()) { it.uppercase() }

  private fun String?.isNonReleaseStatus(): Boolean = this != null && (this in nonReleaseStatuses || this.startsWith("OPT_OUT"))

  private fun String?.toNormalizedStatus(): String? = this?.trim()?.uppercase()

  fun getForBooking(bookingId: Long, licence: Licence): HdcStatus = getForBookingIds(listOf(bookingId), listOf(licence)).first().status

  fun getForBookingIds(
    bookingIds: List<Long>,
    licences: List<Licence>,
  ): List<BookingHdcStatus> {
    val stageByBookingId = licences.associateBy({ it.bookingId }, { it.stage })

    val prisonersById = prisonSearchApiClient
      .getPrisonersByBookingIds(bookingIds)
      .associateBy { it.bookingId.toLong() }

    val hdcStatuses = prisonApiClient
      .getHdcStatuses(bookingIds)
      .associateBy { it.bookingId }

    return buildList {
      bookingIds.forEach { id ->
        val prisoner = prisonersById[id]
        val hdcStatus = hdcStatuses[id]
        val stage = stageByBookingId[id]

        add(
          BookingHdcStatus(
            bookingId = id,
            status = determineHdcStatus(
              prisoner?.homeDetentionCurfewEligibilityDate,
              hdcStatus,
              stage,
            ),
          ),
        )
      }
    }
  }

  private fun determineHdcStatus(
    hdced: LocalDate?,
    prisonerHdcStatus: PrisonerHdcStatus?,
    hdcStage: HdcStage?,
  ): HdcStatus {
    val approvalStatus = prisonerHdcStatus?.approvalStatus.toNormalizedStatus()
    return when {
      hdced == null -> HdcStatus.NOT_A_HDC_RELEASE
      approvalStatus == "APPROVED" -> HdcStatus.APPROVED
      approvalStatus.isNonReleaseStatus() -> HdcStatus.NOT_A_HDC_RELEASE
      hdcStage == null || hdcStage == HdcStage.ELIGIBILITY -> HdcStatus.NOT_STARTED
      hdcStage == HdcStage.PROCESSING_RO -> HdcStatus.ELIGIBILITY_CHECKS_COMPLETE
      else -> HdcStatus.RISK_CHECKS_COMPLETE
    }
  }
}
