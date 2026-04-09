package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Address
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HdcStatusService
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.LicenceConditionRenderer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.LicenceNotFoundForMigrationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAdditionalCondition
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAppointmentAddress
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateAppointmentDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateConditions
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateCurfewTime
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFirstNight
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateFromHdcToCvlRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateLicenceDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateLicenceLifecycleDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateLicenceType
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigratePrisonDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigratePrisonerDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateSentenceDetails
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.request.MigrateStatus
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonSearchApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class MigrationService(
  private val migrationRepository: MigrationRepository,
  private val cvlClient: CvlApiClient,
  private val prisonApiClient: PrisonApiClient,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val auditEventRepository: AuditEventRepository,
  private val hdcStatusService: HdcStatusService,
) {

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

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
  ): MigrateFromHdcToCvlRequest {
    val licenceData = licence.licence ?: throw ValidationException("Licence data must exist for licence id ${licence.id}")
    val audits = auditEventRepository.findByBookingId(licence.bookingId.toString())

    return MigrateFromHdcToCvlRequest(
      bookingNo = prisoner.bookNumber,
      bookingId = licence.bookingId,
      pnc = prisoner.pncNumber,
      cro = prisoner.croNumber,
      prisoner = mapPrisonerDetails(prisoner),
      prison = mapPrisonDetails(prisoner),
      sentence = mapSentenceDetails(prisoner),
      licence = mapLicenceDetails(licence, prisoner, hdcStatus),
      lifecycle = mapLifecycleDetails(audits, hdcStatus),
      conditions = mapConditions(licence, licenceData),
      curfewAddress = mapCurfewAddress(licence, licenceData),
      curfew = mapCurfewDetails(licenceData),
      appointment = mapAppointmentDetails(licenceData),
    )
  }

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

  private fun mapLifecycleDetails(
    audits: List<AuditEvent>,
    hdcStatus: HdcStatus,
  ): MigrateLicenceLifecycleDetails {
    val submitted = getLastAudit(audits, "SEND", "roToCa")
    val approved: AuditEvent? = if (hdcStatus == HdcStatus.APPROVED) getLastAudit(audits, "SEND", "dmToCa") else null

    val created = getFirstUpdateAfterCaToRo(audits)
    val lastUpdated = getLastUpdated(audits)

    return MigrateLicenceLifecycleDetails(
      approvedDate = approved?.timestamp,
      approvedByUsername = approved?.user,
      submittedDate = submitted?.timestamp,
      submittedByUserName = submitted?.user,
      createdByUserName = created?.user,
      dateCreated = created?.timestamp,
      dateLastUpdated = lastUpdated?.timestamp,
      updatedByUsername = lastUpdated?.user,
    )
  }

  private fun mapConditions(licence: Licence, licenceData: LicenceData): MigrateConditions {
    licenceData.licenceConditions?.let { conditions ->
      val additional = LicenceConditionRenderer.renderConditions(licence).map {
        MigrateAdditionalCondition(
          text = it.text!!,
          conditionCode = it.code!!,
          conditionsVersion = 1,
        )
      }
      // Do I only take over approved bespoke conditions? it.approved
      val bespoke = conditions.bespoke?.mapNotNull { it.text } ?: emptyList()
      return MigrateConditions(bespoke = bespoke, additional = additional)
    }
    return MigrateConditions()
  }

  private fun mapCurfewAddress(licence: Licence, licenceData: LicenceData): MigrateAddress {
    val address = getAddress(
      licenceData,
    ) ?: throw ValidationException("Curfew address is null for licence id ${licence.id} this should not migrate to cvl!")
    // Above, Should we check if the address is null? and if so should we throw a validation exception?

    return address.let {
      MigrateAddress(it.addressLine1, it.addressLine2, it.townOrCity, it.postcode)
    }
  }

  private fun mapCurfewDetails(licenceData: LicenceData): MigrateCurfewDetails? = licenceData.curfew?.let {
    MigrateCurfewDetails(
      curfewTimes = it.curfewHours?.let { curfew -> toMigrateCurfewTimes(curfew) },
      firstNight = it.firstNight?.let { fn ->
        MigrateFirstNight(fn.firstNightFrom, fn.firstNightUntil)
      },
    )
  }

  private fun mapAppointmentDetails(licenceData: LicenceData): MigrateAppointmentDetails? = licenceData.reporting?.reportingInstructions?.let { ri ->
    MigrateAppointmentDetails(
      person = ri.name,
      time = toLocalDateTimeOrDate(ri.reportingDate, ri.reportingTime),
      telephone = ri.telephone,
      address = MigrateAppointmentAddress(
        firstLine = ri.buildingAndStreet1,
        secondLine = ri.buildingAndStreet2,
        townOrCity = ri.townOrCity,
        postcode = ri.postcode,
      ),
    )
  }

  private fun toLocalDateTimeOrDate(reportingDate: String?, reportingTime: String?): LocalDateTime? {
    if (reportingDate == null || reportingTime == null) return null

    val date = LocalDate.parse(reportingDate, formatter)
    val time = reportingTime.let { LocalTime.parse(it) } ?: LocalTime.MIDNIGHT
    return LocalDateTime.of(date, time)
  }

  private fun getLastAudit(allAudits: List<AuditEvent>, action: String, transitionType: String): AuditEvent? = allAudits
    .asSequence()
    .filter { audit -> audit.action == action && audit.details["transitionType"]?.toString() == transitionType }
    .lastOrNull()

  private fun getFirstUpdateAfterCaToRo(allAudits: List<AuditEvent>): AuditEvent? {
    val indexOfTransition = allAudits.indexOfFirst { it.details["transitionType"]?.toString() == "caToRo" }
    if (indexOfTransition == -1) return null

    return allAudits.asSequence().drop(indexOfTransition + 1)
      .firstOrNull { audit ->
        audit.action == "UPDATE_SECTION"
      }
  }

  private fun getLastUpdated(allAudits: List<AuditEvent>): AuditEvent? = allAudits
    .lastOrNull { audit ->
      audit.action == "UPDATE_SECTION"
    }

  fun toMigrateCurfewTimes(curfewHours: CurfewHours): List<MigrateCurfewTime> {
    with(curfewHours) {
      val isDaySpecific = daySpecificInputs?.name == "YES"

      if (isDaySpecific) {
        return DayOfWeek.entries.mapNotNull { day ->
          getTime(day, from = true)?.let { fromTime ->
            getTime(day, from = false)?.let { untilTime ->
              val crossesMidnight = untilTime.isBefore(fromTime)
              MigrateCurfewTime(
                fromDay = day,
                fromTime = fromTime,
                untilDay = if (crossesMidnight) day.plus(1) else day,
                untilTime = untilTime,
              )
            }
          }
        }
      } else {
        return listOf(
          MigrateCurfewTime(
            fromTime = this.allFrom!!,
            untilTime = this.allUntil!!,
          ),
        )
      }
    }
  }

  private fun CurfewHours.getTime(day: DayOfWeek, from: Boolean): LocalTime? = when (day) {
    MONDAY -> if (from) mondayFrom else mondayUntil
    TUESDAY -> if (from) tuesdayFrom else tuesdayUntil
    WEDNESDAY -> if (from) wednesdayFrom else wednesdayUntil
    THURSDAY -> if (from) thursdayFrom else thursdayUntil
    FRIDAY -> if (from) fridayFrom else fridayUntil
    SATURDAY -> if (from) saturdayFrom else saturdayUntil
    SUNDAY -> if (from) sundayFrom else sundayUntil
  }

  fun getAddress(licenceData: LicenceData): MigrateAddress? {
    var address: Address? = null
    with(licenceData) {
      licenceData.curfew?.approvedPremisesAddress?.let { address = it }
      if (address == null) {
        licenceData.bassReferral?.approvedPremisesAddress?.let { address = it }
      }
      if (address == null) {
        licenceData.proposedAddress?.curfewAddress?.let { address = it }
      }
      if (address == null) {
        licenceData.bassReferral?.bassOffer?.let { address = it }
      }
    }

    return address?.let {
      MigrateAddress(
        it.addressLine1,
        it.addressLine2,
        it.addressTown,
        it.postCode,
      )
    }
  }
}
