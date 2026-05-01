package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Address
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Licence
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.LicenceConditionRenderer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.LicenceBookingDetail
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

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
class MigrationRequestService(
  private val migrationRepository: MigrationRepository,
  private val cvlClient: CvlApiClient,
  private val prisonApiClient: PrisonApiClient,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val auditEventRepository: AuditEventRepository,
) {

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  fun migrateLicenceToCvl(activeLicenceId: Long) {
    val request = buildMigrationRequest(activeLicenceId)
    request?.let { cvlClient.migrateLicence(request) }
  }

  fun migrateBatchedLicenceToCvl(licenceDetail: LicenceBookingDetail, prisoner: Prisoner) {
    val licence = migrationRepository.findById(licenceDetail.licenceId).get()
    return cvlClient.migrateLicence(createMigrationRequest(licence, prisoner))
  }

  fun buildMigrationRequest(activeLicenceId: Long): MigrateFromHdcToCvlRequest? {
    val licence = getLicence(activeLicenceId)
    val prisoner = performPrisonerSearch(licence.bookingId)
    if (isEligible(prisoner)) {
      return createMigrationRequest(licence, prisoner)
    }
    log.debug("Licence id $activeLicenceId is not eligible for migration")
    return null
  }

  private fun createMigrationRequest(
    licence: Licence,
    prisoner: Prisoner,
  ): MigrateFromHdcToCvlRequest {
    val licenceData = licence.licence ?: throw MigrationValidationException("Licence data must exist for licence id ${licence.id}")
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
      // See isApproved below should we be rejecting the license if not approved? I would say so!
      lifecycle = mapLifecycleDetails(audits, isApproved(licence)),
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

  fun isEligible(prisoner: Prisoner): Boolean {
    with(prisoner) {
      // Null dates are not allowed
      val hdcad = homeDetentionCurfewActualDate ?: return false
      val led = licenceExpiryDate ?: return false
      val tused = topupSupervisionExpiryDate ?: return false

      if (status != "INACTIVE OUT") return false
      if (isRestrictedPatient()) return false

      val today = LocalDate.now()
      if (hdcad.isAfter(today)) return false
      if (led.isBefore(today) && tused.isBefore(today)) return false
    }

    return true
  }

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
          conditionsVersion = licence.additionalConditionsVersion ?: throw MigrationValidationException("additional conditions version not set for licence id ${licence.id} condition code ${it.code}"),
        )
      }
      val bespoke = conditions.bespoke?.mapNotNull { it.text } ?: emptyList()
      return MigrateConditions(bespoke = bespoke, additional = additional)
    }
    return MigrateConditions()
  }

  private fun mapCurfewAddress(licence: Licence, licenceData: LicenceData): MigrateAddress {
    val address = getAddress(licenceData)!!

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
    val id = auditEventRepository.findLicenceRecordStartedAuditId(bookingId.toString()) ?: throw MigrationValidationException("LICENCE_RECORD_STARTED audit id not found for booking id $bookingId")
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
      val isDaySpecific = daySpecificInputs?.name.equals("YES", ignoreCase = true)

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
        return DayOfWeek.entries.map { day ->
          val crossesMidnight = allUntil!!.isBefore(allFrom!!)
          MigrateCurfewTime(
            fromDay = day,
            fromTime = this.allFrom,
            untilDay = if (crossesMidnight) day.plus(1) else day,
            untilTime = this.allUntil,
          )
        }
      }
    }
  }

  private fun performPrisonerSearch(bookingId: Long): Prisoner {
    val bookingIds = listOf(bookingId)
    return prisonSearchApiClient.getPrisonersByBookingIds(bookingIds).firstOrNull() ?: throw MigrationValidationException("Prisoner not found for licence id $bookingId")
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

  private fun getLicence(activeLicenceId: Long): Licence {
    val licence = migrationRepository.getMigratableLicence(activeLicenceId)
      ?: throw MigrationValidationException("No eligible licence found for licence id $activeLicenceId")
    return licence
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
