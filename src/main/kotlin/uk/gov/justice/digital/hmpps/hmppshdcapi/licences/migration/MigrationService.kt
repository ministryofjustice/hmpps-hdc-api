package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatusService
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.LicenceNotFoundForMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAdditionalCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAppointmentAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAppointmentDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAuditDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateConditions
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFromHdcToCvlRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateLicenceDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateLicenceType
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigratePrisonDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigratePrisonerDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateSentenceDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.LocalDate
import java.time.LocalTime

@Service
class MigrationService(
  private val migrationRepository: MigrationRepository,
  private val cvlClient: CvlApiClient,
  private val prisonApiClient: PrisonApiClient,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val hdcStatusService: HdcStatusService,
) {

  @Transactional
  fun migrateToCvl(licenceId: Long) {
    cvlClient.migrateLicence(createMigrationRequest(licenceId))
    migrationRepository.insertMigrationLog(licenceId)
  }

  fun createMigrationRequest(licenceId: Long): MigrateFromHdcToCvlRequest {
    val licence = migrationRepository.findById(licenceId)
      .orElseThrow { LicenceNotFoundForMigrationException(licenceId) }

    val bookingIds = listOf(licence.bookingId)

    val prisoner = prisonSearchApiClient.getPrisonersByBookingIds(bookingIds).firstOrNull()
      ?: throw LicenceNotFoundForMigrationException(licenceId)

    val prisonerHdcStatus = prisonApiClient
      .getHdcStatuses(listOf(licence.bookingId))
      .firstOrNull()

    val hdcStatus = hdcStatusService.determineHdcStatus(
      prisoner.homeDetentionCurfewEligibilityDate,
      prisonerHdcStatus,
      licence.stage,
    )

    return mapToCvlRequest(licence, prisoner, hdcStatus)
  }

  private fun mapToCvlRequest(
    licence: Licence,
    prisoner: Prisoner,
    hdcStatus: HdcStatus,
  ): MigrateFromHdcToCvlRequest = MigrateFromHdcToCvlRequest(
    bookingNo = prisoner.bookNumber,
    bookingId = prisoner.bookingId.toLong(),
    pnc = prisoner.pncNumber,
    cro = prisoner.croNumber,
    prisoner = mapPrisonerDetails(prisoner),
    prison = mapPrisonDetails(prisoner),
    sentence = mapSentenceDetails(prisoner),
    licence = mapLicenceDetails(licence, prisoner, hdcStatus),
    audit = mapAuditDetails(),
    conditions = mapConditions(),
    curfewAddress = mapCurfewAddress(),
    curfew = mapCurfewDetails(),
    appointment = mapAppointmentDetails(),
  )

  private fun mapPrisonerDetails(prisoner: Prisoner) = MigratePrisonerDetails(
    prisonerNumber = prisoner.prisonerNumber,
    forename = prisoner.firstName,
    middleNames = prisoner.middleNames,
    surname = prisoner.lastName,
    dateOfBirth = prisoner.dateOfBirth,
  )

  private fun mapPrisonDetails(prisoner: Prisoner) = MigratePrisonDetails(
    prisonCode = prisoner.prisonId,
    prisonDescription = prisoner.prisonName ?: prisoner.locationDescription,
    prisonTelephone = null,
  )

  private fun mapSentenceDetails(prisoner: Prisoner) = MigrateSentenceDetails(
    startDate = prisoner.sentenceStartDate,
    endDate = prisoner.sentenceExpiryDate,
    conditionalReleaseDate = prisoner.conditionalReleaseDateOverrideDate
      ?: prisoner.conditionalReleaseDate,
    actualReleaseDate = prisoner.confirmedReleaseDate ?: prisoner.releaseDate,
    topupSupervisionStartDate = prisoner.topupSupervisionStartDate,
    topupSupervisionExpiryDate = prisoner.topupSupervisionExpiryDate,
    postRecallReleaseDate = prisoner.postRecallReleaseDate,
  )

  private fun mapLicenceDetails(
    licence: Licence,
    prisoner: Prisoner,
    hdcStatus: HdcStatus,
  ): MigrateLicenceDetails = MigrateLicenceDetails(
    typeCode = MigrateLicenceType.from(licence.licence?.document?.template?.decision),
    statusCode = MigrateStatus.from(hdcStatus),
    hdcLicenceVersion = licence.version.toString(),
    licenceActivationDate = licence.transitionDate?.toLocalDate(),
    licenceExpiryDate = prisoner.licenceExpiryDate,
    homeDetentionCurfewActualDate =
    prisoner.homeDetentionCurfewActualDate ?: prisoner.homeDetentionCurfewEligibilityDate,
    homeDetentionCurfewEndDate = prisoner.homeDetentionCurfewEndDate,
  )

  private fun mapAuditDetails() = MigrateAuditDetails(
    approvedDate = LocalDate.of(2026, 5, 1).atStartOfDay(),
    approvedByUsername = "approver1",
    approvedByName = "Approver Name",
    submittedDate = LocalDate.of(2026, 5, 2).atStartOfDay(),
    submittedByUserName = "submitter1",
    createdByUserName = "creator1",
    dateCreated = LocalDate.of(2026, 5, 3).atStartOfDay(),
    dateLastUpdated = LocalDate.of(2026, 5, 4).atStartOfDay(),
    updatedByUsername = "updater1",
  )

  private fun mapConditions() = MigrateConditions(
    bespoke = listOf("Do not contact victim"),
    additional = listOf(
      MigrateAdditionalCondition(
        text = "Report to probation",
        conditionCode = "AC1",
        conditionsVersion = 1,
      ),
    ),
  )

  private fun mapCurfewAddress() = MigrateAddress(
    addressLine1 = "1 Test Street",
    addressLine2 = "Flat 1",
    townOrCity = "London",
    postcode = "SW1A 1AA",
  )

  private fun mapCurfewDetails() = MigrateCurfewDetails(
    curfewTimes = listOf(
      MigrateCurfewTime(
        fromDay = MONDAY,
        fromTime = LocalTime.of(19, 0),
        untilDay = TUESDAY,
        untilTime = LocalTime.of(7, 0),
        createdTimestamp = LocalDate.of(2026, 5, 6).atStartOfDay(),
      ),
    ),
    firstNight = MigrateFirstNight(
      firstNightFrom = LocalTime.of(18, 0),
      firstNightUntil = LocalTime.of(8, 0),
    ),
  )

  private fun mapAppointmentDetails() = MigrateAppointmentDetails(
    person = "Officer Person",
    time = LocalDate.of(2026, 5, 7).atStartOfDay(),
    telephone = "07123456789",
    address = MigrateAppointmentAddress(
      firstLine = "Probation Office",
      secondLine = "High Street",
      townOrCity = "London",
      postcode = "SW1A 2AA",
    ),
  )
}
