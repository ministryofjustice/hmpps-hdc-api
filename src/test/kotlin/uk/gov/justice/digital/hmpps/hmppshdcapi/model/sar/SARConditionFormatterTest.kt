package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.convertToSentenceCase
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getConditionText
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getConditionValues
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.getPolicyVersion
import uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar.SARConditionFormatter.policyVersions

class SARConditionFormatterTest {

  @Test
  fun `camelCase is converted to sentence case`() {
    assertThat("licenceInCvl".convertToSentenceCase()).isEqualTo("Licence in cvl")
  }

  @Test
  fun `snake_case is converted to sentence case`() {
    assertThat("licence_in_cvl".convertToSentenceCase()).isEqualTo("Licence in cvl")
  }

  @Test
  fun `acronyms are handled correctly`() {
    assertThat("JSONDataVersion".convertToSentenceCase()).isEqualTo("Json data version")
  }

  @Test
  fun `numbers are separated correctly`() {
    assertThat("version2".convertToSentenceCase()).isEqualTo("Version 2")
    assertThat("JSONDataVersion2".convertToSentenceCase()).isEqualTo("Json data version 2")
  }

  @Test
  fun `already spaced string is normalized`() {
    assertThat("Licence in cvl".convertToSentenceCase()).isEqualTo("Licence in cvl")
  }

  @Test
  fun `empty string returns empty`() {
    assertThat("".convertToSentenceCase()).isEqualTo("")
  }

  @Test
  fun `single word is capitalized`() {
    assertThat("licence".convertToSentenceCase()).isEqualTo("Licence")
  }

  @Test
  fun `mixed camel and snake case is handled`() {
    assertThat("licence_inCvlData".convertToSentenceCase()).isEqualTo("Licence in cvl data")
  }

  @Test
  fun `returns null for empty map`() {
    val result = policyVersions.getConditionValues(emptyMap())
    assertThat(result).isNull()
  }

  @Test
  fun `converts multiple mixed keys`() {
    val input = mapOf(
      "licenceInCvl" to true,
      "standard_conditions" to false,
      "JSONDataVersion2" to 2,
    )
    val result = policyVersions.getConditionValues(input)
    assertThat(result).containsExactlyInAnyOrderEntriesOf(
      mapOf(
        "Licence in cvl" to true,
        "Standard conditions" to false,
        "Json data version 2" to 2,
      ),
    )
  }

  @Test
  fun `getConditionText with version returns correct text`() {
    val (version, conditions) = policyVersions.entries.first()
    val result = policyVersions.getConditionText(version, conditions[0].id)
    assertThat(result).isEqualTo(conditions[0].text)
  }

  @Test
  fun `getConditionText with version returns fallback if not found`() {
    val (version) = policyVersions.entries.first()
    val result = policyVersions.getConditionText(version, "nonexistent-id")
    assertThat(result).isEqualTo("nonexistent-id")
  }

  @Test
  fun `getConditionText with no version returns fallback if not found`() {
    val result = policyVersions.getConditionText(null, "nonexistent-id")
    assertThat(result).isEqualTo("nonexistent-id")
  }

  @Test
  fun `getConditionVersion when condition is not unique across policies`() {
    val nonUniqueId = policyVersions.entries.flatMap { it.value }
      .groupingBy { it.id }
      .eachCount()
      .filterValues { it > 1 }
      .keys
      .first()

    val result = policyVersions.getPolicyVersion(listOf(nonUniqueId))
    assertThat(result).isNull()
  }

  @Test
  fun `getConditionVersion returns null when condition not found`() {
    val result = policyVersions.getPolicyVersion(listOf("non-existent-id"))
    assertThat(result).isNull()
  }

  @Test
  fun `getConditionVersion when condition is unique across policies`() {
    val uniqueId = policyVersions.entries.flatMap { it.value }
      .groupingBy { it.id }
      .eachCount()
      .filterValues { it == 1 }
      .keys
      .first()

    val expectedVersion = policyVersions.entries.first { it.value.any { condition -> condition.id == uniqueId } }.key

    val result = policyVersions.getPolicyVersion(listOf(uniqueId))
    assertThat(result).isEqualTo(expectedVersion)
  }
}
