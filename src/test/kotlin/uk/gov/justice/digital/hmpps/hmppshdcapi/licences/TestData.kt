package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Booking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TestData {
  fun aPreferredAddressLicence() = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
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
        addressProposed = AddressProposed(Decision.YES),
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
        ),
      ),
      risk = Risk(
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "sam",
          postcode = "AA BRD",
          telephone = "47450",
          townOrCity = "Test town",
          organisation = "crc",
          reportingDate = "12/12/2023",
          reportingTime = "12:12",
          buildingAndStreet1 = "10",
          buildingAndStreet2 = "street",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aCas2Licence() = Licence(
    id = 2,
    prisonNumber = "T12345D",
    bookingId = 98765,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
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
        ),
      ),
      risk = Risk(
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "Bob Smith",
          postcode = "1111 1AD",
          telephone = "01234 123456",
          townOrCity = "Testvill",
          organisation = "Testvill NPS",
          reportingDate = "28/03/2023",
          reportingTime = "12:00",
          buildingAndStreet1 = "10 NoReal Street",
          buildingAndStreet2 = "",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aCas2LicenceWithShortAddress() = Licence(
    id = 3,
    prisonNumber = "T12345D",
    bookingId = 98765,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
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
        ),
      ),
      risk = Risk(
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "Bob Smith",
          postcode = "1111 1AD",
          telephone = "01234 123456",
          townOrCity = "Testvill",
          organisation = "Testvill NPS",
          reportingDate = "28/03/2023",
          reportingTime = "12:00",
          buildingAndStreet1 = "10 NoReal Street",
          buildingAndStreet2 = "",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aCas2ApprovedPremisesLicence() = Licence(
    id = 4,
    prisonNumber = "T12345D",
    bookingId = 98765,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
        bassRequest = Cas2Request(bassRequested = Decision.YES),
        bassOffer = null,
        approvedPremisesAddress = CurfewAddress(
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
        ),
      ),
      risk = Risk(
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "sam",
          postcode = "AA BRD",
          telephone = "47450",
          townOrCity = "Test town",
          organisation = "crc",
          reportingDate = "12/12/2023",
          reportingTime = "12:12",
          buildingAndStreet1 = "10",
          buildingAndStreet2 = "street",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aCurfewApprovedPremisesRequiredLicence() = Licence(
    id = 5,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
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
        ),
        approvedPremisesAddress = CurfewAddress(
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
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "sam",
          postcode = "AA BRD",
          telephone = "47450",
          townOrCity = "Test town",
          organisation = "crc",
          reportingDate = "12/12/2023",
          reportingTime = "12:12",
          buildingAndStreet1 = "10",
          buildingAndStreet2 = "street",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aLicenceWithSingleMissingCurfewHour() = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
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
        addressProposed = AddressProposed(Decision.YES),
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
        ),
      ),
      risk = Risk(
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "sam",
          postcode = "AA BRD",
          telephone = "47450",
          townOrCity = "Test town",
          organisation = "crc",
          reportingDate = "12/12/2023",
          reportingTime = "12:12",
          buildingAndStreet1 = "10",
          buildingAndStreet2 = "street",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aLicenceWithMultipleMissingCurfewHours() = Licence(
    id = 1,
    prisonNumber = "A12345B",
    bookingId = 54321,
    stage = "MODIFIED",
    version = 1,
    transitionDate = LocalDateTime.of(2023, 10, 22, 10, 15),
    varyVersion = 0,
    additionalConditionsVersion = null,
    standardConditionsVersion = null,
    deletedAt = null,
    licenceInCvl = false,
    licence = LicenceData(
      eligibility = Eligibility(
        crdTime = DecisionMade(Decision.NO),
        excluded = DecisionMade(Decision.NO),
        suitability = DecisionMade(Decision.NO),
      ),
      bassReferral = Cas2Referral(
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
        addressProposed = AddressProposed(Decision.YES),
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
        ),
      ),
      risk = Risk(
        riskManagement = RiskManagement(
          version = "3",
          emsInformation = Decision.NO,
          pomConsultation = Decision.YES,
          mentalHealthPlan = Decision.NO,
          unsuitableReason = "",
          hasConsideredChecks = Decision.YES,
          manageInTheCommunity = Decision.YES,
          emsInformationDetails = "",
          riskManagementDetails = "",
          proposedAddressSuitable = Decision.YES,
          awaitingOtherInformation = Decision.NO,
          nonDisclosableInformation = Decision.NO,
          nonDisclosableInformationDetails = "",
          manageInTheCommunityNotPossibleReason = "",
        ),
      ),
      reporting = Reporting(
        reportingInstructions = ReportingInstructions(
          name = "sam",
          postcode = "AA BRD",
          telephone = "47450",
          townOrCity = "Test town",
          organisation = "crc",
          reportingDate = "12/12/2023",
          reportingTime = "12:12",
          buildingAndStreet1 = "10",
          buildingAndStreet2 = "street",
        ),
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
        onRemand = OnRemand(
          decision = Decision.NO,
        ),
        seriousOffence = SeriousOffence(
          decision = Decision.NO,
        ),
        confiscationOrder = ConfiscationOrder(
          decision = Decision.NO,
        ),
      ),
    ),
  )

  fun aBooking() = Booking(
    offenderNo = "A12345B",
    bookingId = 54321,
    agencyId = "MDI",
    topupSupervisionExpiryDate = LocalDate.of(2024, 8, 14),
    licenceExpiryDate = LocalDate.of(2024, 8, 14),
  )
}
