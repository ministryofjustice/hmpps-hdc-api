package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.PrisonerHdcStatus
import java.time.LocalDateTime
import java.time.LocalTime

object TestData {
  val riskManagement = RiskManagementV3(
    version = "3",
    emsInformation = "No",
    pomConsultation = "Yes",
    mentalHealthPlan = "No",
    unsuitableReason = "",
    hasConsideredChecks = "Yes",
    manageInTheCommunity = "Yes",
    emsInformationDetails = "",
    riskManagementDetails = "",
    proposedAddressSuitable = "Yes",
    awaitingOtherInformation = "No",
    nonDisclosableInformation = "No",
    nonDisclosableInformationDetails = "",
    manageInTheCommunityNotPossibleReason = "",
  )

  val reportingInstructions = ReportingInstructions(
    name = "sam",
    postcode = "AA BRD",
    telephone = "47450",
    townOrCity = "Test town",
    organisation = "crc",
    reportingDate = "12/12/2023",
    reportingTime = "12:12",
    buildingAndStreet1 = "10",
    buildingAndStreet2 = "street",
  )

  val curfewHours = CurfewHours(
    mondayFrom = LocalTime.of(19, 0),
    mondayUntil = LocalTime.of(7, 0),
    tuesdayFrom = LocalTime.of(19, 0),
    tuesdayUntil = LocalTime.of(7, 0),
    wednesdayFrom = LocalTime.of(19, 0),
    wednesdayUntil = LocalTime.of(7, 0),
    thursdayFrom = LocalTime.of(19, 0),
    thursdayUntil = LocalTime.of(7, 0),
    fridayFrom = LocalTime.of(19, 0),
    fridayUntil = LocalTime.of(7, 0),
    saturdayFrom = LocalTime.of(19, 0),
    saturdayUntil = LocalTime.of(7, 0),
    sundayFrom = LocalTime.of(19, 0),
    sundayUntil = LocalTime.of(7, 0),
    allFrom = null,
    allUntil = null,
  )

  val eligibility = Eligibility(
    crdTime = CrdTime(Decision.NO),
    excluded = Excluded(Decision.NO),
    suitability = Suitability(Decision.NO),
  )

  fun aPreferredAddressLicence(): Licence = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.NO),
        bassOffer = null,
      ),
      proposedAddress = ProposedAddress(
        curfewAddress = CurfewAddress(
          addressLine1 = "1 The Street",
          addressLine2 = "Area",
          addressTown = "Town",
          postCode = "TS7 7TS",
        ),
        addressProposed = DecisionMade(Decision.YES),
      ),
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(16, 0), firstNightUntil = LocalTime.of(8, 0)),
        curfewHours = CurfewHours(
          mondayFrom = LocalTime.of(20, 0),
          mondayUntil = LocalTime.of(8, 0),
          tuesdayFrom = LocalTime.of(20, 0),
          tuesdayUntil = LocalTime.of(8, 0),
          wednesdayFrom = LocalTime.of(20, 0),
          wednesdayUntil = LocalTime.of(8, 0),
          thursdayFrom = LocalTime.of(20, 0),
          thursdayUntil = LocalTime.of(8, 0),
          fridayFrom = LocalTime.of(20, 0),
          fridayUntil = LocalTime.of(8, 0),
          saturdayFrom = LocalTime.of(20, 0),
          saturdayUntil = LocalTime.of(8, 0),
          sundayFrom = LocalTime.of(20, 0),
          sundayUntil = LocalTime.of(8, 0),
          allFrom = null,
          allUntil = null,
        ),
      ),
      risk = Risk(
        riskManagement = riskManagement,
      ),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          comments = "Some comments",
          confiscationUnitConsulted = Decision.YES,
        ),
      ),
    ),
  )

  fun aCas2Licence() = Licence(
    id = 2,
    prisonNumber = "T12345D",
    bookingId = 98765,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = Cas2Offer(
          addressLine1 = "2 The Street",
          addressLine2 = "Area 2",
          addressTown = "Town 2",
          postCode = "TS6 6TS",
          bassAccepted = OfferAccepted.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(15, 0), firstNightUntil = LocalTime.of(7, 0)),
        curfewHours = CurfewHours(
          mondayFrom = LocalTime.of(19, 0),
          mondayUntil = LocalTime.of(7, 0),
          tuesdayFrom = LocalTime.of(19, 0),
          tuesdayUntil = LocalTime.of(7, 0),
          wednesdayFrom = LocalTime.of(19, 0),
          wednesdayUntil = LocalTime.of(7, 0),
          thursdayFrom = LocalTime.of(19, 0),
          thursdayUntil = LocalTime.of(7, 0),
          fridayFrom = LocalTime.of(19, 0),
          fridayUntil = LocalTime.of(7, 0),
          saturdayFrom = LocalTime.of(19, 0),
          saturdayUntil = LocalTime.of(7, 0),
          sundayFrom = LocalTime.of(19, 0),
          sundayUntil = LocalTime.of(7, 0),
          allFrom = null,
          allUntil = null,
        ),
      ),
      risk = Risk(
        riskManagement = riskManagement,
      ),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          comments = null,
          confiscationUnitConsulted = null,
        ),
      ),
    ),
  )

  fun aCas2LicenceWithShortAddress() = Licence(
    id = 3,
    prisonNumber = "T12345D",
    bookingId = 98765,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = Cas2Offer(
          addressLine1 = "2 The Street",
          addressLine2 = null,
          addressTown = "Town 2",
          postCode = "TS6 6TS",
          bassAccepted = OfferAccepted.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(15, 0), firstNightUntil = LocalTime.of(7, 0)),
        curfewHours = curfewHours,
      ),
      risk = Risk(
        riskManagement = riskManagement,
      ),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          confiscationUnitConsulted = null,
          comments = null,
        ),
      ),
    ),
  )

  fun aCas2ApprovedPremisesLicence() = Licence(
    id = 4,
    prisonNumber = "T12345D",
    bookingId = 98765,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = null,
        approvedPremisesAddress = AddressAndPhone(
          addressLine1 = "3 The Avenue",
          addressLine2 = "Area 3",
          addressTown = "Town 3",
          postCode = "TS5 5TS",
        ),
        bassAreaCheck = Cas2AreaCheck(
          approvedPremisesRequiredYesNo = Decision.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(15, 0), firstNightUntil = LocalTime.of(7, 0)),
        curfewHours = curfewHours,
      ),
      risk = Risk(
        riskManagement = riskManagement,
      ),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          comments = null,
          confiscationUnitConsulted = null,
        ),
      ),
    ),
  )

  fun aCurfewApprovedPremisesRequiredLicence() = Licence(
    id = 5,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.NO),
        bassOffer = null,
        approvedPremisesAddress = null,
        bassAreaCheck = Cas2AreaCheck(
          approvedPremisesRequiredYesNo = Decision.NO,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(16, 0), firstNightUntil = LocalTime.of(8, 0)),
        curfewHours = CurfewHours(
          mondayFrom = LocalTime.of(20, 0),
          mondayUntil = LocalTime.of(8, 0),
          tuesdayFrom = LocalTime.of(20, 0),
          tuesdayUntil = LocalTime.of(8, 0),
          wednesdayFrom = LocalTime.of(20, 0),
          wednesdayUntil = LocalTime.of(8, 0),
          thursdayFrom = LocalTime.of(20, 0),
          thursdayUntil = LocalTime.of(8, 0),
          fridayFrom = LocalTime.of(20, 0),
          fridayUntil = LocalTime.of(8, 0),
          saturdayFrom = LocalTime.of(20, 0),
          saturdayUntil = LocalTime.of(8, 0),
          sundayFrom = LocalTime.of(20, 0),
          sundayUntil = LocalTime.of(8, 0),
          allFrom = null,
          allUntil = null,
        ),
        approvedPremisesAddress = AddressAndPhone(
          addressLine1 = "4 The Street",
          addressLine2 = "Area 4",
          addressTown = "Town 4",
          postCode = "TS4 4TS",
        ),
        approvedPremises = ApprovedPremises(
          required = Decision.YES,
        ),
      ),
      risk = Risk(
        riskManagement = riskManagement,
      ),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          comments = null,
          confiscationUnitConsulted = null,
        ),
      ),
    ),
  )

  fun aLicenceWithSingleMissingCurfewHour() = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.NO),
        bassOffer = null,
      ),
      proposedAddress = ProposedAddress(
        curfewAddress = CurfewAddress(
          addressLine1 = "1 The Street",
          addressLine2 = "Area",
          addressTown = "Town",
          postCode = "TS7 7TS",
        ),
        addressProposed = DecisionMade(Decision.YES),
      ),
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(16, 0), firstNightUntil = LocalTime.of(8, 0)),
        curfewHours = CurfewHours(
          mondayFrom = LocalTime.of(20, 0),
          mondayUntil = null,
          tuesdayFrom = LocalTime.of(20, 0),
          tuesdayUntil = LocalTime.of(8, 0),
          wednesdayFrom = LocalTime.of(20, 0),
          wednesdayUntil = LocalTime.of(8, 0),
          thursdayFrom = LocalTime.of(20, 0),
          thursdayUntil = LocalTime.of(8, 0),
          fridayFrom = LocalTime.of(20, 0),
          fridayUntil = LocalTime.of(8, 0),
          saturdayFrom = LocalTime.of(20, 0),
          saturdayUntil = LocalTime.of(8, 0),
          sundayFrom = LocalTime.of(20, 0),
          sundayUntil = LocalTime.of(8, 0),
          allFrom = null,
          allUntil = null,
        ),
      ),
      risk = Risk(riskManagement = riskManagement),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          comments = null,
          confiscationUnitConsulted = null,
        ),
      ),
    ),
  )

  fun aLicenceWithMultipleMissingCurfewHours() = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = HdcStage.MODIFIED,
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = eligibility,
      bassReferral = CurrentCas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.NO),
        bassOffer = null,
      ),
      proposedAddress = ProposedAddress(
        curfewAddress = CurfewAddress(
          addressLine1 = "1 The Street",
          addressLine2 = "Area",
          addressTown = "Town",
          postCode = "TS7 7TS",
        ),
        addressProposed = DecisionMade(Decision.YES),
      ),
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(16, 0), firstNightUntil = LocalTime.of(8, 0)),
        curfewHours = CurfewHours(
          mondayFrom = LocalTime.of(20, 0),
          mondayUntil = null,
          tuesdayFrom = LocalTime.of(20, 0),
          tuesdayUntil = LocalTime.of(8, 0),
          wednesdayFrom = LocalTime.of(20, 0),
          wednesdayUntil = LocalTime.of(8, 0),
          thursdayFrom = LocalTime.of(20, 0),
          thursdayUntil = null,
          fridayFrom = LocalTime.of(20, 0),
          fridayUntil = LocalTime.of(8, 0),
          saturdayFrom = LocalTime.of(20, 0),
          saturdayUntil = LocalTime.of(8, 0),
          sundayFrom = LocalTime.of(20, 0),
          sundayUntil = LocalTime.of(8, 0),
          allFrom = null,
          allUntil = null,
        ),
      ),
      risk = Risk(
        riskManagement = riskManagement,
      ),
      reporting = Reporting(
        reportingInstructions = reportingInstructions,
      ),
      victim = Victim(
        victimLiaison = VictimLiaison(
          decision = Decision.NO,
        ),
      ),
      licenceConditions = LicenceConditions(
        bespoke = emptyList(),
        standard = Standard(
          additionalConditionsRequired = Decision.NO,
        ),
        additional = null,
        conditionsSummary = ConditionsSummary(
          additionalConditionsJustification = "",
        ),
      ),
      document = Document(
        template = Template(
          decision = "hdc_ap",
          offenceCommittedBeforeFeb2015 = Decision.NO,
        ),
      ),
      approval = Approval(
        release = Release(
          decision = Decision.YES,
          decisionMaker = "Test McWell",
          reasonForDecision = "",
        ),
      ),
      finalChecks = FinalChecks(
        onRemand = DecisionMade(
          decision = Decision.NO,
        ),
        seriousOffence = DecisionMade(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
          comments = null,
          confiscationUnitConsulted = null,
        ),
      ),
    ),
  )

  fun aLicenceVersion() = LicenceVersion(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    timestamp = LocalDateTime.of(2023, 10, 22, 10, 15),
    version = 1,
    template = "hdc_ap",
    varyVersion = 0,
    deletedAt = null,
    licenceInCvl = false,
    licence = aCas2Licence().licence,
  )

  fun hdcPrisonerStatus() = PrisonerHdcStatus(
    approvalStatus = "REJECTED",
    bookingId = 1,
    passed = true,
  )
}
