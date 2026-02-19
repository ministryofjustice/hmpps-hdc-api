package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class V1ConditionsRendererTest {

  @Test
  fun `REPORT_TO renders correctly`() {
    // Given
    val additionalData = mapOf(
      "REPORTTO" to mapOf(
        "reportingTime" to "TEST_TIME",
        "reportingDaily" to "TEST_DAILY",
        "reportingAddress" to "TEST_ADDRESS",
        "reportingFrequency" to "TEST_FREQUENCY",

      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Report to staff at TEST_ADDRESS at TEST_TIME, unless otherwise authorised by your supervising officer.  This condition will be reviewed by your supervising officer on a TEST_FREQUENCY basis and may be amended or removed if it is felt that the level of risk you present has reduced appropriately.",
    )
    assertThat(conditionIds).containsExactly("REPORTTO")
  }

  @Test
  fun `NOCONTACTPRISONER renders correctly`() {
    // Given
    val additionalData = mapOf("NOCONTACTPRISONER" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to contact directly or indirectly any person who is a serving or remand offender or detained in State custody, without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NOCONTACTPRISONER")
  }

  @Test
  fun `NOCONTACTASSOCIATE renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NOCONTACTASSOCIATE" to mapOf("groupsOrOrganisation" to "GROUPS_OR_ORGANISATION_VALUE"),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to associate with any person currently or formerly associated with GROUPS_OR_ORGANISATION_VALUE without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NOCONTACTASSOCIATE")
  }

  @Test
  fun `NOCONTACTSEXOFFENDER renders correctly`() {
    // Given
    val additionalData = mapOf("NOCONTACTSEXOFFENDER" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to contact or associate with a known sex offender other than when compelled by attendance at a Treatment Programme or when residing at Approved Premises without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NOCONTACTSEXOFFENDER")
  }

  @Test
  fun `INTIMATERELATIONSHIP renders correctly`() {
    // Given
    val additionalData = mapOf("INTIMATERELATIONSHIP" to mapOf("intimateGender" to "MEN_OR_WOMEN"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Notify your supervising officer of any developing intimate relationships with MEN_OR_WOMEN.",
    )
    assertThat(conditionIds).containsExactly("INTIMATERELATIONSHIP")
  }

  @Test
  fun `NOCONTACTNAMED renders correctly`() {
    // Given
    val additionalData = mapOf("NOCONTACTNAMED" to mapOf("noContactOffenders" to "NAMED_OFFENDER_VALUE"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to contact or associate with NAMED_OFFENDER_VALUE without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NOCONTACTNAMED")
  }

  @Test
  fun `NORESIDE renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NORESIDE" to mapOf(
        "notResideWithGender" to "ANY MALE",
        "notResideWithAge" to "TEST_AGE_VALUE",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to reside (not even to stay for one night) in the same household as ANY MALE child under the age of TEST_AGE_VALUE without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NORESIDE")
  }

  @Test
  fun `NOUNSUPERVISEDCONTACT renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NOUNSUPERVISEDCONTACT" to mapOf(
        "unsupervisedContactAge" to "TEST_AGE_VALUE",
        "unsupervisedContactGender" to "ANY FEMALE",
        "unsupervisedContactSocial" to "TEST_SOCIAL_SERVICE",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to have unsupervised contact with  ANY FEMALE children under the age of TEST_AGE_VALUE without the prior approval of your supervising officer and / or TEST_SOCIAL_SERVICE except where that contact is inadvertent and not reasonably avoidable in the course of lawful daily life.",
    )
    assertThat(conditionIds).containsExactly("NOUNSUPERVISEDCONTACT")
  }

  @Test
  fun `NOCHILDRENSAREA renders correctly`() {
    // Given
    val additionalData = mapOf("NOCHILDRENSAREA" to mapOf("notInSightOf" to "PLAYGROUND"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to enter or remain in sight of any PLAYGROUND without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NOCHILDRENSAREA")
  }

  @Test
  fun `NOWORKWITHAGE renders correctly`() {
    // Given
    val additionalData = mapOf("NOWORKWITHAGE" to mapOf("noWorkWithAge" to "16"))
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to undertake work or other organised activity which will involve a person under the age of 16, either on a paid or unpaid basis without the prior approval of your supervising officer.",
    )
    assertThat(conditionIds).containsExactly("NOWORKWITHAGE")
  }

  @Test
  fun `NOTIFYRELATIONSHIP renders correctly`() {
    // Given
    val additionalData = mapOf("NOTIFYRELATIONSHIP" to emptyMap<String, String>())
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Notify your supervising officer of any developing personal relationships, whether intimate or not, with any person you know or believe to be resident in a household containing children under the age of 18. This includes persons known to you prior to your time in custody with whom you are renewing or developing a personal relationship with.",
    )
    assertThat(conditionIds).containsExactly("NOTIFYRELATIONSHIP")
  }

  @Test
  fun `NOCOMMUNICATEVICTIM renders correctly`() {
    // Given
    val additionalData = mapOf(
      "NOCOMMUNICATEVICTIM" to mapOf(
        "victimFamilyMembers" to "VICTIM_NAME",
        "socialServicesDept" to "SOCIAL_SERVICE_NAME",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Not to seek to approach or communicate with VICTIM_NAME without the prior approval of your supervising officer and / or SOCIAL_SERVICE_NAME.",
    )
    assertThat(conditionIds).containsExactly("NOCOMMUNICATEVICTIM")
  }

  @Test
  fun `ATTENDDEPENDENCYINDRUGSSECTION renders correctly`() {
    // Given
    val additionalData = mapOf(
      "ATTENDDEPENDENCYINDRUGSSECTION" to mapOf(
        "appointmentDateInDrugsSection" to "2026-02-06",
        "appointmentTimeInDrugsSection" to "10:00",
        "appointmentAddressInDrugsSection" to "123 Drug Centre",
      ),
    )
    val licence = createLicence(createLicenceData(createConditions(additionalData)))

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)

    // Then
    val renderedTexts = result.map { it.text }
    val conditionIds = result.map { it.code }
    assertThat(renderedTexts).containsExactly(
      "Attend 123 Drug Centre on 2026-02-06 at 10:00, as directed, to address your dependency on, or propensity to misuse, a controlled drug.",
    )
    assertThat(conditionIds).containsExactly("ATTENDDEPENDENCYINDRUGSSECTION")
  }
}
