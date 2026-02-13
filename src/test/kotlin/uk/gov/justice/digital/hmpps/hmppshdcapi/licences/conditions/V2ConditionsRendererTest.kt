package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class V2ConditionsRendererTest {

  @Test
  fun `REPORT_TO renders correctly`() {
    // Given
    val additionalData = mapOf(
      "REPORT_TO" to mapOf(
        "reportingTime" to "TEST_TIME",
        "reportingDaily" to "TEST_DAILY",
        "reportingAddress" to "TEST_ADDRESS",
        "reportingFrequency" to "TEST_FREQUENCY",

      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Report to staff at TEST_ADDRESS at TEST_TIME, unless otherwise authorised by your supervising officer.  This condition will be reviewed by your supervising officer on a TEST_FREQUENCY basis and may be amended or removed if it is felt that the level of risk you present has reduced appropriately",
    )
    assertThat(conditionIds).containsExactly("REPORT_TO")
  }

  @Test
  fun `RESIDE_AT_SPECIFIC_PLACE renders correctly`() {
    // Given
    val additionalData = mapOf("RESIDE_AT_SPECIFIC_PLACE" to mapOf("region" to "TEST_REGION"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "You must reside within TEST_REGION while of no fixed abode, unless otherwise approved by your supervising officer",
    )
    assertThat(conditionIds).containsExactly("RESIDE_AT_SPECIFIC_PLACE")
  }

  @Test
  fun `NO_RESIDE renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NO_RESIDE" to mapOf(
        "notResideWithGender" to "ANY FEMALE",
        "notResideWithAge" to "TEST_AGE_VALUE",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to reside (not even to stay for one night) in the same household as ANY FEMALE child under the age of TEST_AGE_VALUE without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_RESIDE")
  }

  @Test
  fun `ATTEND_ALL renders correctly`() {
    // Given
    val additionalData = mapOf("ATTEND_ALL" to mapOf("appointmentProfessions" to "PSYCHIATRIST"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Attend all appointments arranged for you with a PSYCHIATRIST and co-operate fully with any care or treatment they recommend",
    )
    assertThat(conditionIds).containsExactly("ATTEND_ALL")
  }

  @Test
  fun `HOME_VISITS renders correctly`() {
    // Given
    val additionalData = mapOf("HOME_VISITS" to mapOf("mentalHealthName" to "MENTAL_HEALTH_WORKER"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Receive home visits from MENTAL_HEALTH_WORKER Mental Health Worker",
    )
    assertThat(conditionIds).containsExactly("HOME_VISITS")
  }

  @Test
  fun `ATTEND_DEPENDENCY_IN_DRUGS_SECTION renders correctly`() {
    // Given
    val additionalData = mapOf(
      "ATTEND_DEPENDENCY_IN_DRUGS_SECTION" to mapOf(
        "appointmentDateInDrugsSection" to "2026-01-01",
        "appointmentTimeInDrugsSection" to "09:00",
        "appointmentAddressInDrugsSection" to "DRUGS_CENTRE",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Attend DRUGS_CENTRE on 2026-01-01 at 09:00, as directed, to address your dependency on, or propensity to misuse, a controlled drug",
    )
    assertThat(conditionIds).containsExactly("ATTEND_DEPENDENCY_IN_DRUGS_SECTION")
  }

  @Test
  fun `NO_COMMUNICATE_VICTIM renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NO_COMMUNICATE_VICTIM" to mapOf(
        "victimFamilyMembers" to "FAMILY_MEMBER",
        "socialServicesDept" to "SOCIAL_SERVICES",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to seek to approach or communicate with FAMILY_MEMBER without the prior approval of your supervising officer and / or SOCIAL_SERVICES",
    )
    assertThat(conditionIds).containsExactly("NO_COMMUNICATE_VICTIM")
  }

  @Test
  fun `RETURN_TO_UK renders correctly`() {
    // Given
    val additionalData = mapOf("RETURN_TO_UK" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Should you return to the UK and Islands before the expiry date of your licence then your licence conditions will be in force and you must report within two working days to our supervising officer",
    )
    assertThat(conditionIds).containsExactly("RETURN_TO_UK")
  }

  @Test
  fun `NO_UNSUPERVISED_CONTACT renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NO_UNSUPERVISED_CONTACT" to mapOf(
        "unsupervisedContactAge" to "TEST_AGE",
        "unsupervisedContactGender" to "ANY MALE",
        "unsupervisedContactSocial" to "SOCIAL_SERVICE",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to have unsupervised contact with  ANY MALE children under the age of TEST_AGE without the prior approval of your supervising officer and / or SOCIAL_SERVICE except where that contact is inadvertent and not reasonably avoidable in the course of lawful daily life",
    )
    assertThat(conditionIds).containsExactly("NO_UNSUPERVISED_CONTACT")
  }

  @Test
  fun `NO_CONTACT_NAMED renders correctly`() {
    // Given
    val additionalData = mapOf("NO_CONTACT_NAMED" to mapOf("noContactOffenders" to "OFFENDER_NAME"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to contact or associate with OFFENDER_NAME without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_CONTACT_NAMED")
  }

  @Test
  fun `NO_CONTACT_SEX_OFFENDER renders correctly`() {
    // Given
    val additionalData = mapOf("NO_CONTACT_SEX_OFFENDER" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to contact or associate with a known sex offender other than when compelled by attendance at a Treatment Programme or when residing at Approved Premises without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_CONTACT_SEX_OFFENDER")
  }

  @Test
  fun `NO_CONTACT_PRISONER renders correctly`() {
    // Given
    val additionalData = mapOf("NO_CONTACT_PRISONER" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to contact directly or indirectly any person who is a serving or remand prisoner or detained in State custody, without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_CONTACT_PRISONER")
  }

  @Test
  fun `NO_CONTACT_ASSOCIATE renders correctly`() {
    // Given
    val additionalData = mapOf("NO_CONTACT_ASSOCIATE" to mapOf("groupsOrOrganisation" to "SPECIFIC_GROUP"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to associate with any person currently or formerly associated with SPECIFIC_GROUP without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_CONTACT_ASSOCIATE")
  }

  @Test
  fun `COMPLY_REQUIREMENTS renders correctly`() {
    // Given
    val additionalData = mapOf(
      "COMPLY_REQUIREMENTS" to mapOf(
        "abuseAndBehaviours" to "drug",
        "courseOrCentre" to "TEST_CENTRE",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "To comply with any requirements specified by your supervising officer for the purpose of ensuring that you address your drug problems at the TEST_CENTRE",
    )
    assertThat(conditionIds).containsExactly("COMPLY_REQUIREMENTS")
  }

  @Test
  fun `NO_WORK_WITH_AGE renders correctly`() {
    // Given
    val additionalData = mapOf("NO_WORK_WITH_AGE" to mapOf("noWorkWithAge" to "16"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to undertake work or other organised activity which will involve a person under the age of 16, either on a paid or unpaid basis without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_WORK_WITH_AGE")
  }

  @Test
  fun `ONE_PHONE renders correctly`() {
    // Given
    val additionalData = mapOf("ONE_PHONE" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to own or possess more than one mobile phone or SIM card without the prior approval of your supervising officer and to provide your supervising officer with details of that mobile telephone or one you have regular use of, including the IMEI number and the SIM card that you possess",
    )
    assertThat(conditionIds).containsExactly("ONE_PHONE")
  }

  @Test
  fun `NO_CAMERA_PHONE renders correctly`() {
    // Given
    val additionalData = mapOf("NO_CAMERA_PHONE" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to own or possess a mobile phone with a photographic function without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NO_CAMERA_PHONE")
  }

  @Test
  fun `CAMERA_APPROVAL renders correctly`() {
    // Given
    val additionalData = mapOf("CAMERA_APPROVAL" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "Not to own or use a camera without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("CAMERA_APPROVAL")
  }

  @Test
  fun `NO_CAMERA renders correctly`() {
    // Given
    val additionalData = mapOf("NO_CAMERA" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "To make any device capable of making or storing digital images (including a camera and a mobile phone with a camera function) available for inspection on request by your supervising officer and/or a police officer",
    )
    assertThat(conditionIds).containsExactly("NO_CAMERA")
  }

  @Test
  fun `SURRENDER_PASSPORT renders correctly`() {
    // Given
    val additionalData = mapOf("SURRENDER_PASSPORT" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)), additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }
    assertThat(renderedTexts).containsExactly(
      "To surrender your passport(s) to your supervising officer and to notify your supervising officer of any intention to apply for a new passport",
    )
    assertThat(conditionIds).containsExactly("SURRENDER_PASSPORT")
  }
}
