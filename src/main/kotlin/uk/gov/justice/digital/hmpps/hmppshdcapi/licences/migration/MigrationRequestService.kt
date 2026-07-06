package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.Address
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AuditEventRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.CurfewHours
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceData
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions.LicenceConditionRenderer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.client.CvlApiClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.exceptions.MigrationValidationException
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.LicenceBookingDetail
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationLicenceVersion
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
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.AddressType
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.attemptToGuessVersion
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
import java.time.format.DateTimeParseException

enum class LicenceType {
  LICENCE,
  VARIATION_LICENCE,
  VARIATION_LICENCE_FROM_OUT_OF_SYSTEM,
  NOT_KNOWN,
}

class LicenceTypeRecord(
  val type: LicenceType,
  val auditFromId: Long,
  val licenceRecordStarted: Long? = null,
  val varyLicenceRecordStarted: Long? = null,
)

@Transactional(propagation = Propagation.NEVER)
@Service
class MigrationRequestService(
  private val migrationRepository: MigrationRepository,
  private val cvlClient: CvlApiClient,
  private val prisonApiClient: PrisonApiClient,
  private val prisonSearchApiClient: PrisonSearchApiClient,
  private val auditEventRepository: AuditEventRepository,
) {

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  fun migrateLicenceToCvl(licenceDetail: LicenceBookingDetail, prisoner: Prisoner) {
    log.info("HDC migration: Migrating licence version id {} to CVL", licenceDetail.licenceVersionId)
    val licenceVersion = migrationRepository.getLicenceVersion(licenceDetail.licenceVersionId)
    return cvlClient.migrateLicence(createMigrationRequest(licenceVersion, prisoner))
  }

  fun buildMigrationRequestForPreview(activeLicenceId: Long): MigrateFromHdcToCvlRequest? {
    val licenceVersion = getMigratableLicenceVersionForPreview(activeLicenceId)
    val prisoner = performPrisonerSearch(licenceVersion.bookingId)
    return createMigrationRequest(licenceVersion, prisoner)
  }

  private fun createMigrationRequest(
    licenceVersion: MigrationLicenceVersion,
    prisoner: Prisoner,
  ): MigrateFromHdcToCvlRequest {
    val licenceType = getLicencesType(licenceVersion.bookingId)
    val licenceData = extractLicenceDataFromJson(licenceVersion)
    val lifecycleDetails = if (licenceType.type != LicenceType.NOT_KNOWN) mapLifecycleDetails(licenceVersion, licenceType) else null

    validate(licenceData, licenceVersion, licenceType, lifecycleDetails)

    return MigrateFromHdcToCvlRequest(
      bookingNo = prisoner.bookNumber,
      bookingId = licenceVersion.bookingId,
      pnc = prisoner.pncNumber,
      cro = prisoner.croNumber,
      prisoner = mapPrisonerDetails(prisoner),
      prison = mapPrisonDetails(prisoner),
      sentence = mapSentenceDetails(prisoner),
      licence = mapLicenceDetails(licenceVersion, prisoner),
      lifecycle = lifecycleDetails!!,
      conditions = mapConditions(licenceData, licenceVersion),
      curfewAddress = mapCurfewAddress(licenceData),
      curfew = mapCurfewDetails(licenceData),
      appointment = mapAppointmentDetails(licenceData),
    )
  }

  private fun extractLicenceDataFromJson(licenceVersion: MigrationLicenceVersion): LicenceData {
    try {
      return mapper.readValue(licenceVersion.licenceJson, LicenceData::class.java)
    } catch (e: DatabindException) {
      throw MigrationValidationException("JSON Parse exception, ${e.message}")
    }
  }

  private fun mapPrisonerDetails(prisoner: Prisoner) = MigratePrisonerDetails(
    prisonerNumber = prisoner.prisonerNumber,
    forename = prisoner.firstName,
    middleNames = prisoner.middleNames,
    surname = prisoner.lastName,
    dateOfBirth = prisoner.dateOfBirth,
  )

  private fun mapPrisonDetails(prisoner: Prisoner) = MigratePrisonDetails(
    prisonCode = prisoner.lastPrisonId ?: throw MigrationValidationException("Prison code not found for prisoner: ${prisoner.prisonerNumber}"),
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

  fun validate(prisoner: Prisoner) {
    log.info("HDC migration: Validating licence eligibility for prisoner: {}", prisoner.prisonerNumber)
    fun notEligible(reason: String): Unit = throw MigrationValidationException(reason)

    with(prisoner) {
      if (status != "INACTIVE OUT") notEligible("Licence has invalid status: $status")
      if (isRestrictedPatient()) notEligible("Licence has restricted patient")

      val today = LocalDate.now()
      homeDetentionCurfewActualDate?.let {
        if (it.isAfter(today)) notEligible("Licence has HDCAD in the future: $it")
      } ?: notEligible("Licence has missing HDCAD date")
      licenceExpiryDate?.let {
        if (it.isBefore(today)) notEligible("Licence expiry date is in past: LED=$it")
      } ?: notEligible("Missing licence expiry date")
    }
  }

  fun validate(
    licenceData: LicenceData,
    licence: MigrationLicenceVersion,
    licenceTypeRecord: LicenceTypeRecord,
    lifecycleDetails: MigrateLicenceLifecycleDetails? = null,
  ) {
    if (licenceData.licenceConditions?.additional?.isNotEmpty() == true) {
      attemptToGuessVersion(licenceData.licenceConditions, licence)
        ?: throw MigrationValidationException("Licence additional conditions version not determined!")
    }
    validateCurfewAddress(licenceData)
    validateIfVariationHasUnapprovedChanges(licence)

    if (LicenceType.NOT_KNOWN == licenceTypeRecord.type) {
      throw MigrationValidationException("No users information found in audit")
    }

    lifecycleDetails?.let {
      val missing = buildList {
        if (it.createdByUserName.isNullOrBlank()) add("creator")
        if (it.submittedByUserName.isNullOrBlank()) add("submitter")
        if (it.approvedByUsername.isNullOrBlank() && it.approvedByName.isNullOrBlank()) add("approver")
      }

      if (missing.isNotEmpty()) {
        throw MigrationValidationException(
          "Missing lifecycle users: ${missing.joinToString(", ")}",
        )
      }
    }
  }

  private fun validateIfVariationHasUnapprovedChanges(licence: MigrationLicenceVersion) {
    val licenceWithUnApprovedChanges = migrationRepository.findLicenceWithUnApprovedChanges(
      licence.bookingId,
      licence.version,
      licence.varyVersion,
    )
    licenceWithUnApprovedChanges?.let {
      val currentVersion = "${it.version}.${it.varyVersion}"
      val approvedVersion = "${licence.version}.${licence.varyVersion}"

      throw MigrationValidationException(
        "Found a licence at stage ${it.stage} with unapproved changes " +
          "(current version $currentVersion, approved version $approvedVersion).",
      )
    }
  }

  fun validateCurfewAddress(licenceData: LicenceData) {
    findCurfewAddress(licenceData) ?: throw MigrationValidationException("No valid curfew address found")
  }

  fun attemptToGuessVersion(licenceConditions: LicenceConditions, licence: MigrationLicenceVersion): Int? {
    licenceConditions.additional.let {
      var version = attemptToGuessVersion(it)
      if (version == null && it?.size == 1 && it.containsKey("POLYGRAPH")) {
        // Text is the same on all versions
        version = 2
      }
      if (version == null) {
        // We should only get here when we have additional conditions of POLYGRAPH and DRUG_TESTING or just DRUG_TESTING
        version = migrationRepository.getConditionsVersionFor(licence.bookingId)
        log.debug(
          "HDC migration: used licence to get conditions version {} for licence version id {}",
          version,
          licence.id,
        )
      }
      return version
    }
  }

  private fun mapLicenceDetails(
    licenceVersion: MigrationLicenceVersion,
    prisoner: Prisoner,
  ): MigrateLicenceDetails = MigrateLicenceDetails(
    licenceVersionId = licenceVersion.id,
    typeCode = MigrateLicenceType.from(licenceVersion.template),
    licenceActivationDate = prisoner.homeDetentionCurfewActualDate ?: prisoner.confirmedReleaseDate ?: prisoner.releaseDate,
    licenceExpiryDate = prisoner.licenceExpiryDate,
    homeDetentionCurfewActualDate = prisoner.homeDetentionCurfewActualDate,
    homeDetentionCurfewEndDate = prisoner.homeDetentionCurfewEndDate,
    licenceVersion = licenceVersion.version,
    varyVersion = licenceVersion.varyVersion,
  )

  private fun mapLifecycleDetails(
    licenceVersion: MigrationLicenceVersion,
    licenceType: LicenceTypeRecord,
  ): MigrateLicenceLifecycleDetails {
    val audits = getAuditsForLatestLicence(licenceVersion.bookingId, licenceType.auditFromId)

    return if (LicenceType.LICENCE == licenceType.type) {
      createLifecycleDetailsFromLicence(audits)
    } else {
      createLifecycleDetailsFromVariation(audits)
    }
  }

  private fun createLifecycleDetailsFromVariation(
    audits: List<AuditEvent>,
  ): MigrateLicenceLifecycleDetails {
    val submitted = getLastAuditByDetails(audits, "UPDATE_SECTION", detailsContains = "/hdc/vary/approval/")
    val createdBy = getLastAuditByDetails(audits, "VARY_NOMIS_LICENCE_CREATED")

    val approvedByName = (submitted?.details["userInput"] as? Map<*, *>)?.get("name") as? String

    return MigrateLicenceLifecycleDetails(
      approvedDate = submitted?.timestamp,
      approvedByName = approvedByName,
      submittedDate = submitted?.timestamp,
      submittedByUserName = submitted?.user,
      createdByUserName = createdBy?.user,
      dateCreated = createdBy?.timestamp,
    )
  }

  private fun createLifecycleDetailsFromLicence(
    audits: List<AuditEvent>,
  ): MigrateLicenceLifecycleDetails {
    val submitted = getLastAuditByTransitionType(audits, "SEND", "roToCa")
    val approved = getLastAuditByTransitionType(audits, "SEND", "dmToCa")
    val createdBy = getFirstUpdateAfterCaToRo(audits)

    return MigrateLicenceLifecycleDetails(
      approvedDate = approved?.timestamp,
      approvedByUsername = approved?.user,
      submittedDate = submitted?.timestamp,
      submittedByUserName = submitted?.user,
      createdByUserName = createdBy?.user,
      dateCreated = createdBy?.timestamp,
    )
  }

  private fun mapConditions(licenceData: LicenceData, licenceVersion: MigrationLicenceVersion): MigrateConditions {
    licenceData.licenceConditions?.let { conditions ->

      val additional = mapAdditionalConditions(conditions, licenceData, licenceVersion)

      val bespoke = conditions.bespoke?.mapNotNull { it.text } ?: emptyList()
      return MigrateConditions(bespoke = bespoke, additional = additional)
    }

    return MigrateConditions()
  }

  private fun mapAdditionalConditions(
    conditions: LicenceConditions,
    licenceData: LicenceData,
    licenceVersion: MigrationLicenceVersion,
  ): MutableList<MigrateAdditionalCondition> {
    val additional = mutableListOf<MigrateAdditionalCondition>()

    if (conditions.additional?.isNotEmpty() == true) {
      val conditionsVersion = attemptToGuessVersion(conditions, licenceVersion)!!
      LicenceConditionRenderer.renderConditions(licenceData, conditionsVersion).forEach {
        additional.add(
          MigrateAdditionalCondition(
            text = it.text!!,
            conditionCode = it.code!!,
            conditionsVersion = conditionsVersion,
          ),
        )
      }
    }

    return additional
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

  fun toLocalDateTimeOrDate(reportingDate: String?, reportingTime: String?): LocalDateTime? {
    if (reportingDate.isNullOrBlank() || reportingTime.isNullOrBlank()) return null
    var date: LocalDate
    try {
      date = LocalDate.parse(reportingDate, formatter)
    } catch (e: DateTimeParseException) {
      throw MigrationValidationException("Invalid date format: $reportingDate")
    }

    var time: LocalTime
    try {
      time = reportingTime.let { LocalTime.parse(it) }
    } catch (e: DateTimeParseException) {
      throw MigrationValidationException("Invalid time format: $reportingTime")
    }

    return LocalDateTime.of(date, time)
  }

  private fun getLastAuditByTransitionType(allAudits: List<AuditEvent>, action: String, transitionType: String): AuditEvent? = allAudits
    .asSequence()
    .filter { audit -> audit.action == action && audit.details["transitionType"]?.toString() == transitionType }
    .lastOrNull()

  private fun getLastAuditByDetails(allAudits: List<AuditEvent>, action: String, detailsContains: String? = null): AuditEvent? = allAudits
    .asSequence()
    .filter { audit -> audit.action == action && (detailsContains == null || audit.details.values.any { it.toString().contains(detailsContains) }) }
    .lastOrNull()

  private fun getLicencesType(bookingId: Long): LicenceTypeRecord {
    val licenceRecordStarted = auditEventRepository.findLicenceRecordStartedAuditId(bookingId.toString())
    val varyLicenceRecordStarted = auditEventRepository.findVaryLicenceFromOutOfSystemAuditId(bookingId.toString())
    val type = when {
      licenceRecordStarted != null && varyLicenceRecordStarted != null -> LicenceType.VARIATION_LICENCE
      varyLicenceRecordStarted != null -> LicenceType.VARIATION_LICENCE_FROM_OUT_OF_SYSTEM
      licenceRecordStarted != null -> LicenceType.LICENCE
      else -> LicenceType.NOT_KNOWN
    }

    val auditFromId = varyLicenceRecordStarted ?: licenceRecordStarted ?: -1
    return LicenceTypeRecord(type, auditFromId, licenceRecordStarted, varyLicenceRecordStarted)
  }

  private fun getAuditsForLatestLicence(bookingId: Long, auditFromId: Long): List<AuditEvent> = auditEventRepository.findByBookingIdAndAuditId(bookingId.toString(), auditFromId)

  fun getFirstUpdateAfterCaToRo(allAudits: List<AuditEvent>): AuditEvent? {
    val indexOfTransition = allAudits.indexOfFirst { it.details["transitionType"]?.toString() == "caToRo" }
    if (indexOfTransition == -1) return null

    return allAudits.asSequence().drop(indexOfTransition + 1)
      .firstOrNull { audit ->
        audit.action == "UPDATE_SECTION"
      }
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
        if (allUntil != null && allFrom != null) {
          return DayOfWeek.entries.map { day ->
            val crossesMidnight = allUntil.isBefore(allFrom)
            MigrateCurfewTime(
              fromDay = day,
              fromTime = this.allFrom,
              untilDay = if (crossesMidnight) day.plus(1) else day,
              untilTime = this.allUntil,
            )
          }
        }
        return emptyList()
      }
    }
  }

  fun performPrisonerSearch(bookingId: Long): Prisoner {
    val bookingIds = listOf(bookingId)
    return prisonSearchApiClient.getPrisonersByBookingIds(bookingIds).firstOrNull() ?: throw MigrationValidationException("Prisoner not found for booking id $bookingId")
  }

  private fun isApproved(licenceVersion: MigrationLicenceVersion): Boolean {
    val prisonerHdcStatus = prisonApiClient
      .getHdcStatuses(listOf(licenceVersion.bookingId))
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

  private fun findCurfewAddress(licenceData: LicenceData): Pair<Address, AddressType>? = with(licenceData) {
    listOf(
      curfew?.approvedPremisesAddress to AddressType.CAS,
      bassReferral?.approvedPremisesAddress to AddressType.CAS,
      proposedAddress?.curfewAddress to AddressType.RESIDENTIAL,
      bassReferral?.bassOffer to AddressType.CAS,
    ).firstOrNull { (address, _) ->
      address?.let(::isValidAddress) == true
    }?.let { (address, addressType) ->
      requireNotNull(address) to addressType
    }
  }

  fun mapCurfewAddress(licenceData: LicenceData): MigrateAddress {
    val (address, addressType) = findCurfewAddress(licenceData)!!

    return MigrateAddress(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      townOrCity = address.addressTown,
      postcode = address.postCode,
      addressType = addressType,
    )
  }

  fun isValidAddress(address: Address): Boolean = listOf(
    address.addressLine1,
    address.addressTown,
    address.postCode,
  ).count { !it.isNullOrBlank() } > 0

  private fun getMigratableLicenceVersionForPreview(activeLicenceId: Long): MigrationLicenceVersion {
    val licenceVersion = migrationRepository.getMigratableLicenceVersionForPreview(activeLicenceId)
      ?: throw MigrationValidationException("No eligible licence found for licence version id $activeLicenceId")
    return licenceVersion
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    val mapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()
  }
}
