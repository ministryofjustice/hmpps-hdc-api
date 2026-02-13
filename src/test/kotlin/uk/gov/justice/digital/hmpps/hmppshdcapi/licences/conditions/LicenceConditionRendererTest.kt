package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LicenceConditionRendererTest {

  @Test
  fun `renderConditions replaces placeholders with additional data`() {
    // Given
    val additionalData = mapOf(
      "NOCONTACTASSOCIATE" to mapOf(
        "groupsOrOrganisation" to "GROUPS_OR_ORGANISATION_VALUE",
      ),
      "NOUNSUPERVISEDCONTACT" to mapOf(
        "unsupervisedContactAge" to "TEST_AGE_VALUE_1",
        "unsupervisedContactGender" to "TEST_GENDER_VALUE_0",
        "unsupervisedContactSocial" to "TEST_SOCIAL_VALUE_2",
      ),
    )

    val licenceConditions = createConditions(additionalData)
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 1)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).contains(
      "Not to have unsupervised contact with  TEST_GENDER_VALUE_0 children under the age of TEST_AGE_VALUE_1 without the prior approval of your supervising officer and / or TEST_SOCIAL_VALUE_2 except where that contact is inadvertent and not reasonably avoidable in the course of lawful daily life",
      "Not to associate with any person currently or formerly associated with GROUPS_OR_ORGANISATION_VALUE without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactlyInAnyOrder("NOUNSUPERVISEDCONTACT", "NOCONTACTASSOCIATE")
  }

  @Test
  fun `report renderConditions replaces placeholders with additional data for additionalConditionsVersion one`() {
    // Given
    val additionalData = mapOf(
      "REPORTTO" to mapOf(
        "reportingTime" to "(1)",
        "reportingDaily" to "(2)",
        "reportingAddress" to "(0)",
        "reportingFrequency" to "(3)",
      ),
    )

    val licenceConditions = createConditions(additionalData)
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 1)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).contains(
      "Report to staff at (0) at (1), unless otherwise authorised by your supervising officer.  This condition will be reviewed by your supervising officer on a (3) basis and may be amended or removed if it is felt that the level of risk you present has reduced appropriately",
    )
    assertThat(conditionIds).containsExactly("REPORTTO")
  }

  @Test
  fun `report renderConditions missing fields gracefully`() {
    // Given
    val additionalData = mapOf(
      "REPORTTO" to mapOf(
        "reportingDaily" to "(2)",
        "reportingAddress" to "(0)",
      ),
    )

    val licenceConditions = createConditions(additionalData)
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 1)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).contains(
      "Report to staff at (0) at (2), unless otherwise authorised by your supervising officer.  This condition will be reviewed by your supervising officer on a  basis and may be amended or removed if it is felt that the level of risk you present has reduced appropriately",
    )
    assertThat(conditionIds).containsExactly("REPORTTO")
  }

  @Test
  fun `report REPORT_TO special case missing reportingDaily field`() {
    // Given
    val additionalData = mapOf(
      "REPORT_TO" to mapOf(
        "reportingTime" to "(1)",
        "reportingAddress" to "(0)",
        "reportingFrequency" to "(3)",
      ),
    )

    val licenceConditions = createConditions(additionalData)
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).contains(
      "Report to staff at (0) at (1), unless otherwise authorised by your supervising officer.  This condition will be reviewed by your supervising officer on a (3) basis and may be amended or removed if it is felt that the level of risk you present has reduced appropriately",
    )
    assertThat(conditionIds).containsExactly("REPORT_TO")
  }

  @Test
  fun `renderConditions replaces placeholders with additional data for additionalConditionsVersion one single condition`() {
    // Given
    val conditionId = "NOCONTACTASSOCIATE"
    val additionalData = mapOf(
      conditionId to mapOf(
        "groupsOrOrganisation" to "GROUPS_OR_ORGANISATION_VALUE",
      ),
    )

    val licenceConditions = createConditions(additionalData)
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 1)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).containsExactly(
      "Not to associate with any person currently or formerly associated with GROUPS_OR_ORGANISATION_VALUE without the prior approval of your supervising officer",
    )
    assertThat(conditionIds).containsExactly("NOCONTACTASSOCIATE")
  }

  @Test
  fun `renderConditions replaces placeholders with additional data for additionalConditionsVersion two`() {
    // Given
    val conditionId = "RESIDE_AT_SPECIFIC_PLACE"
    val additionalData = mapOf(
      conditionId to mapOf(
        "region" to "REGION_VALUE",
      ),
    )

    val licenceConditions = createConditions(additionalData)
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 2)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).containsExactly(
      "You must reside within REGION_VALUE while of no fixed abode, unless otherwise approved by your supervising officer",
    )
    assertThat(conditionIds).containsExactly("RESIDE_AT_SPECIFIC_PLACE")
  }

  @Test
  fun `renderConditions handles missing additional data gracefully`() {
    // Given
    val licenceConditions = createConditions(emptyMap())
    val licenceData = createLicenceData(licenceConditions)
    val licence = createLicence(licenceData, additionalConditionsVersion = 1)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).isEmpty()
    assertThat(conditionIds).isEmpty()
  }

  @Test
  fun `renderConditions returns empty list when licence field is null`() {
    // Given
    val licence = createLicence(licenceData = null)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).isEmpty()
    assertThat(conditionIds).isEmpty()
  }

  @Test
  fun `renderConditions returns empty list when licenceConditions are null`() {
    // Given
    val licenceData = createLicenceData(licenceConditions = null)
    val licence = createLicence(licenceData)

    // When
    val result = LicenceConditionRenderer.renderConditions(licence)
    val renderedTexts = result.map { it.second }
    val conditionIds = result.map { it.first }

    // Then
    assertThat(renderedTexts).isEmpty()
    assertThat(conditionIds).isEmpty()
  }

  @Test
  fun `renderDataToText converts array values to comma separated string`() {
    // Given
    val arrayValue = arrayOf("NOTE1", "NOTE2", "NOTE3")
    val textTemplate = "[notes]"

    // When
    val renderedText = LicenceConditionRenderer.renderDataToText(
      textTemplate,
      listOf(arrayValue),
    )

    // Then
    assertThat(renderedText).isEqualTo("NOTE1, NOTE2, NOTE3")
  }

  @Test
  fun `renderDataToText converts array of one value to comma separated string`() {
    // Given
    val arrayValue = arrayOf("NOTE1")
    val textTemplate = "[notes]"

    // When
    val renderedText = LicenceConditionRenderer.renderDataToText(
      textTemplate,
      listOf(arrayValue),
    )

    // Then
    assertThat(renderedText).isEqualTo("NOTE1")
  }

  @Test
  fun `renderDataToText converts empty array to comma separated string`() {
    // Given
    val arrayValue = arrayOf<String>()
    val textTemplate = "[notes]"

    // When
    val renderedText = LicenceConditionRenderer.renderDataToText(
      textTemplate,
      listOf(arrayValue),
    )

    // Then
    assertThat(renderedText).isEmpty()
  }
}
