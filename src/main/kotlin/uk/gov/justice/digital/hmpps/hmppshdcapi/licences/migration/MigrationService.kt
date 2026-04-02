package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class MigrationService(
  private val cvlClient: CvlApiClient,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun migrateToCvl(licenceId: Long) {
    log.info("Starting migration for licenceId={}", licenceId)

    val request = mapToCvlRequest()
    cvlClient.migrateLicence(request)

    log.info("Ending migration for licenceId={}", licenceId)
  }

  private fun mapToCvlRequest(): MigrateFromHdcToCvlRequest = MigrateFromHdcToCvlRequest(
    bookingNo = "A1234BC",
    bookingId = 123456L,
    pnc = "PNC123",
    cro = "CRO456",
    prisoner = mapPrisonerDetails(),
    prison = mapPrisonDetails(),
    sentence = mapSentenceDetails(),
    licence = mapLicenceDetails(),
    audit = mapAuditDetails(),
    conditions = mapConditions(),
    curfewAddress = mapCurfewAddress(),
    curfew = mapCurfewDetails(),
    appointment = mapAppointmentDetails(),
  )

  private fun mapPrisonerDetails() = MigratePrisonerDetails(
    prisonerNumber = "A1234BC",
    forename = "John",
    middleNames = "Michael",
    surname = "Doe",
    dateOfBirth = LocalDate.of(1990, 1, 1),
  )

  private fun mapPrisonDetails() = MigratePrisonDetails(
    prisonCode = "MDI",
    prisonDescription = "Moorland",
    prisonTelephone = "0123456789",
  )

  private fun mapSentenceDetails() = MigrateSentenceDetails(
    startDate = LocalDate.now().minusYears(1),
    endDate = LocalDate.now().plusYears(1),
    conditionalReleaseDate = LocalDate.now().plusMonths(6),
    actualReleaseDate = LocalDate.now().plusMonths(6),
    topupSupervisionStartDate = null,
    topupSupervisionExpiryDate = null,
    postRecallReleaseDate = null,
  )

  private fun mapLicenceDetails() = MigrateLicenceDetails(
    typeCode = MigrateLicenceType.AP,
    statusCode = MigrateStatus.APPROVED,
    hdcLicenceVersion = "1",
    licenceActivationDate = LocalDate.now(),
    licenceExpiryDate = LocalDate.now().plusYears(1),
    homeDetentionCurfewActualDate = LocalDate.now(),
    homeDetentionCurfewEndDate = LocalDate.now().plusMonths(6),
  )

  private fun mapAuditDetails() = MigrateAuditDetails(
    approvedDate = LocalDateTime.now(),
    approvedByUsername = "approver1",
    approvedByName = "Approver Name",
    submittedDate = LocalDateTime.now().minusDays(1),
    submittedByUserName = "submitter1",
    createdByUserName = "creator1",
    dateCreated = LocalDateTime.now().minusDays(2),
    dateLastUpdated = LocalDateTime.now(),
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
        fromDay = DayOfWeek.MONDAY,
        fromTime = LocalTime.of(19, 0),
        untilDay = DayOfWeek.TUESDAY,
        untilTime = LocalTime.of(7, 0),
        createdTimestamp = LocalDateTime.now(),
      ),
    ),
    firstNight = MigrateFirstNight(
      firstNightFrom = LocalTime.of(18, 0),
      firstNightUntil = LocalTime.of(8, 0),
    ),
  )

  private fun mapAppointmentDetails() = MigrateAppointmentDetails(
    person = "Officer Smith",
    time = LocalDateTime.now().plusDays(1),
    telephone = "07123456789",
    address = MigrateAppointmentAddress(
      firstLine = "Probation Office",
      secondLine = "High Street",
      townOrCity = "London",
      postcode = "SW1A 2AA",
    ),
  )
}
