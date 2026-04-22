package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Address
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.LicenceConditionRenderer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
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
import kotlin.jvm.optionals.getOrNull

@Service
class MigrationService(
  private val migrationRepository: MigrationRepository,
  private val cvlClient: CvlApiClient,
  private val prisonApiClient: PrisonApiClient,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val auditEventRepository: AuditEventRepository,
) {

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  @Transactional
  fun migrateToCvl(activeLicenceId: Long) {
    val request = buildMigrationRequest(activeLicenceId)
    cvlClient.migrateLicence(request)
    migrationRepository.insertMigrationLog(activeLicenceId)
  }

  @Transactional
  fun buildMigrationRequest(activeLicenceId: Long): MigrateFromHdcToCvlRequest {
    val licence = migrationRepository.findById(activeLicenceId).getOrNull()
    val prisoner: Prisoner? = licence?.let { performPrisonerSearch(it) }

    validate(activeLicenceId, licence, prisoner)

    return createMigrationRequest(
      licence!!,
      prisoner!!,
      isApproved(licence),
    )
  }

  private fun validate(licenceId: Long, licence: Licence?, prisoner: Prisoner?) {
    if (licence == null) {
      throw ValidationException("Licence not found for licence id $licenceId")
    }
    if (prisoner == null) {
      throw ValidationException("Prisoner not found for licence id $licenceId ")
    }
  }

  private fun createMigrationRequest(
    licence: Licence,
    prisoner: Prisoner,
    approved: Boolean,
  ): MigrateFromHdcToCvlRequest {
    val licenceData = licence.licence ?: throw ValidationException("Licence data must exist for licence id ${licence.id}")
    val audits = getAuditsForLatestLicence(licence.bookingId)

    return MigrateFromHdcToCvlRequest(
      bookingNo = prisoner.bookNumber,
      bookingId = licence.bookingId,
      pnc = prisoner.pncNumber,
      cro = prisoner.croNumber,
      prisoner = mapPrisonerDetails(prisoner),
      prison = mapPrisonDetails(prisoner),
      sentence = mapSentenceDetails(prisoner),
      licence = mapLicenceDetails(licence, prisoner),
      lifecycle = mapLifecycleDetails(audits, approved),
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
    sentenceStartDate = prisoner.sentenceStartDate,
    sentenceEndDate = prisoner.sentenceExpiryDate,
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
  ): MigrateLicenceDetails = MigrateLicenceDetails(
    licenceId = licence.id!!,
    typeCode = MigrateLicenceType.from(licence.licence?.document?.template?.decision),
    licenceActivationDate = prisoner.homeDetentionCurfewActualDate ?: prisoner.confirmedReleaseDate ?: prisoner.releaseDate,
    licenceExpiryDate = prisoner.licenceExpiryDate,
    homeDetentionCurfewActualDate = prisoner.homeDetentionCurfewActualDate,
    homeDetentionCurfewEndDate = prisoner.homeDetentionCurfewEndDate,
    licenceVersion = licence.version,
    varyVersion = licence.varyVersion,
  )

  private fun mapLifecycleDetails(
    audits: List<AuditEvent>,
    approved: Boolean,
  ): MigrateLicenceLifecycleDetails {
    val submitted = getLastAudit(audits, "SEND", "roToCa")
    val approved: AuditEvent? = if (approved) getLastAudit(audits, "SEND", "dmToCa") else null

    val created = getFirstUpdateAfterCaToRo(audits)
    val lastUpdated = getLastUpdated(audits)

    return MigrateLicenceLifecycleDetails(
      approvedDate = approved?.timestamp,
      approvedByUsername = approved?.user,
      submittedDate = submitted?.timestamp,
      submittedByUserName = submitted?.user,
      createdByUserName = created?.user,
      dateCreated = created?.timestamp,
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
      val bespoke = conditions.bespoke?.mapNotNull { it.text } ?: emptyList()
      return MigrateConditions(bespoke = bespoke, additional = additional)
    }
    return MigrateConditions()
  }

  private fun mapCurfewAddress(licence: Licence, licenceData: LicenceData): MigrateAddress {
    val address = getAddress(
      licenceData,
    ) ?: throw ValidationException("Curfew address is null for licence id ${licence.id} this should not migrate to cvl!")

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
    val time = reportingTime.let { LocalTime.parse(it) }
    return LocalDateTime.of(date, time)
  }

  private fun getLastAudit(allAudits: List<AuditEvent>, action: String, transitionType: String): AuditEvent? = allAudits
    .asSequence()
    .filter { audit -> audit.action == action && audit.details["transitionType"]?.toString() == transitionType }
    .lastOrNull()

  private fun getAuditsForLatestLicence(bookingId: Long): List<AuditEvent> {
    val id = auditEventRepository.findLicenceRecordStartedAuditId(bookingId.toString()) ?: error("LICENCE_RECORD_STARTED audit id not found for booking id $bookingId")
    return auditEventRepository.findByBookingIdAndAuditId(bookingId.toString(), id)
  }

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

  private fun performPrisonerSearch(licence: Licence): Prisoner? {
    val bookingIds = listOf(licence.bookingId)
    return prisonSearchApiClient.getPrisonersByBookingIds(bookingIds).firstOrNull()
  }

  private fun isApproved(licence: Licence): Boolean {
    val prisonerHdcStatus = prisonApiClient
      .getHdcStatuses(listOf(licence.bookingId))
      .firstOrNull()

    return prisonerHdcStatus?.isApproved() == true
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
      address = when {
        curfew?.approvedPremisesAddress != null -> curfew.approvedPremisesAddress
        bassReferral?.approvedPremisesAddress != null -> bassReferral.approvedPremisesAddress
        proposedAddress?.curfewAddress != null -> proposedAddress.curfewAddress
        bassReferral?.bassOffer != null -> bassReferral.bassOffer
        else -> null
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
