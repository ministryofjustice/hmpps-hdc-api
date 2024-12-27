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
        CurfewAddress(
          addressLine1 = "1 The Street",
          addressLine2 = "Area",
          addressTown = "Town",
          postCode = "AB1 2CD",
        ),
      ),
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(16, 0), firstNightUntil = LocalTime.of(8, 0)),
        curfewHours = CurfewHours(
          mondayFrom = "20:00",
          mondayUntil = "08:00",
          tuesdayFrom = "20:00",
          tuesdayUntil = "08:00",
          wednesdayFrom = "20:00",
          wednesdayUntil = "08:00",
          thursdayFrom = "20:00",
          thursdayUntil = "08:00",
          fridayFrom = "20:00",
          fridayUntil = "08:00",
          saturdayFrom = "20:00",
          saturdayUntil = "08:00",
          sundayFrom = "20:00",
          sundayUntil = "08:00",
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
          townOrCity = "Sheffield",
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
          decisionMaker = "Tim Mccluskey",
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
    prisonNumber = "C56789D",
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
          postCode = "EF3 4GH",
          bassAccepted = OfferAccepted.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(15, 0), firstNightUntil = LocalTime.of(7, 0)),
        curfewHours = CurfewHours(
          mondayFrom = "19:00",
          mondayUntil = "07:00",
          tuesdayFrom = "19:00",
          tuesdayUntil = "07:00",
          wednesdayFrom = "19:00",
          wednesdayUntil = "07:00",
          thursdayFrom = "19:00",
          thursdayUntil = "07:00",
          fridayFrom = "19:00",
          fridayUntil = "07:00",
          saturdayFrom = "19:00",
          saturdayUntil = "07:00",
          sundayFrom = "19:00",
          sundayUntil = "07:00",
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
          townOrCity = "Blackburn",
          organisation = "Blackburn NPS",
          reportingDate = "28/03/2023",
          reportingTime = "12:00",
          buildingAndStreet1 = "10 York Street",
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
          decisionMaker = "Tim Mccluskey",
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
    prisonNumber = "C56789D",
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
          postCode = "EF3 4GH",
          bassAccepted = OfferAccepted.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(15, 0), firstNightUntil = LocalTime.of(7, 0)),
        curfewHours = CurfewHours(
          mondayFrom = "19:00",
          mondayUntil = "07:00",
          tuesdayFrom = "19:00",
          tuesdayUntil = "07:00",
          wednesdayFrom = "19:00",
          wednesdayUntil = "07:00",
          thursdayFrom = "19:00",
          thursdayUntil = "07:00",
          fridayFrom = "19:00",
          fridayUntil = "07:00",
          saturdayFrom = "19:00",
          saturdayUntil = "07:00",
          sundayFrom = "19:00",
          sundayUntil = "07:00",
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
          townOrCity = "Blackburn",
          organisation = "Blackburn NPS",
          reportingDate = "28/03/2023",
          reportingTime = "12:00",
          buildingAndStreet1 = "10 York Street",
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
          decisionMaker = "Tim Mccluskey",
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
    prisonNumber = "C56789D",
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
          postCode = "IJ3 4KL",
        ),
        bassAreaCheck = Cas2AreaCheck(
          approvedPremisesRequiredYesNo = Decision.YES,
        ),
      ),
      proposedAddress = null,
      curfew = Curfew(
        firstNight = FirstNight(firstNightFrom = LocalTime.of(15, 0), firstNightUntil = LocalTime.of(7, 0)),
        curfewHours = CurfewHours(
          mondayFrom = "19:00",
          mondayUntil = "07:00",
          tuesdayFrom = "19:00",
          tuesdayUntil = "07:00",
          wednesdayFrom = "19:00",
          wednesdayUntil = "07:00",
          thursdayFrom = "19:00",
          thursdayUntil = "07:00",
          fridayFrom = "19:00",
          fridayUntil = "07:00",
          saturdayFrom = "19:00",
          saturdayUntil = "07:00",
          sundayFrom = "19:00",
          sundayUntil = "07:00",
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
          townOrCity = "Sheffield",
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
          decisionMaker = "Tim Mccluskey",
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
          mondayFrom = "20:00",
          mondayUntil = "08:00",
          tuesdayFrom = "20:00",
          tuesdayUntil = "08:00",
          wednesdayFrom = "20:00",
          wednesdayUntil = "08:00",
          thursdayFrom = "20:00",
          thursdayUntil = "08:00",
          fridayFrom = "20:00",
          fridayUntil = "08:00",
          saturdayFrom = "20:00",
          saturdayUntil = "08:00",
          sundayFrom = "20:00",
          sundayUntil = "08:00",
        ),
        approvedPremisesAddress = CurfewAddress(
          addressLine1 = "4 The Street",
          addressLine2 = "Area 4",
          addressTown = "Town 4",
          postCode = "MN4 5OP",
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
          townOrCity = "Sheffield",
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
          decisionMaker = "Tim Mccluskey",
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
