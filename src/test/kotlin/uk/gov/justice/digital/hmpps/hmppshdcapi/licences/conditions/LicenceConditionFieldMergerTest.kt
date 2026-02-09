package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.conditions

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test

class LicenceConditionFieldMergerTest {

  private val condenser = LicenceConditionFieldMerger()

  @Test
  fun `should condense appointmentDetails correctly and in order of rule not field position with prison appointment`() {
    // Given
    val fieldPosition = mapOf("appointmentDate" to 0, "appointmentTime" to 1, "appointmentAddress" to 2)
    val conditionMetaData = aConditionMetadata(userInput = "appointmentDetails", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "appointmentAddress" to "HMP Leeds, Wing B",
      "appointmentDate" to "2026-02-15",
      "appointmentTime" to "09:30",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("appointmentDate", "HMP Leeds, Wing B on 2026-02-15 at 09:30"),
    )
  }

  @Test
  fun `should remove original fields after condensing with sample collection`() {
    // Given
    val fieldPosition = mapOf("attendSampleDetailsName" to 0, "attendSampleDetailsAddress" to 1)
    val conditionMetaData = aConditionMetadata(userInput = "attendSampleDetails", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "attendSampleDetailsName" to "Officer Smith",
      "attendSampleDetailsAddress" to "HMP Manchester, Medical Unit",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("attendSampleDetailsName", "Officer Smith, HMP Manchester, Medical Unit"),
    )
  }

  @Test
  fun `should return same map if userInput has no rule`() {
    // Given
    val conditionMetaData = aConditionMetadata(userInput = "unknownField")
    val additionalFields = mapOf("someField" to "Restricted Area")

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result)
      .isEqualTo(additionalFields as Map<String, Any?>)
  }

  @Test
  fun `should handle partially missing fields for prison appointment`() {
    // Given
    val fieldPosition = mapOf("appointmentAddress" to 0, "appointmentDate" to 1, "appointmentTime" to 2)
    val conditionMetaData = aConditionMetadata(userInput = "appointmentDetails", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "appointmentAddress" to "HMP Durham, Wing C",
      "appointmentTime" to "11:00",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("appointmentAddress", "HMP Durham, Wing C at 11:00"),
    )
  }

  @Test
  fun `should handle empty field values for prison appointment`() {
    // Given
    val fieldPosition = mapOf("appointmentAddress" to 0, "appointmentDate" to 1, "appointmentTime" to 2)
    val conditionMetaData = aConditionMetadata(userInput = "appointmentDetails", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "appointmentAddress" to "HMP Hull, Wing D",
      "appointmentDate" to "",
      "appointmentTime" to "13:00",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("appointmentAddress", "HMP Hull, Wing D at 13:00"),
    )
  }

  @Test
  fun `should condense drug_testing correctly with prison data`() {
    // Given
    val fieldPosition = mapOf("drug_testing_name" to 0, "drug_testing_address" to 1)
    val conditionMetaData = aConditionMetadata(userInput = "drug_testing", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "drug_testing_name" to "Officer Brown",
      "drug_testing_address" to "HMP Wandsworth, Testing Lab",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("drug_testing_name", "Officer Brown, HMP Wandsworth, Testing Lab"),
    )
  }

  @Test
  fun `should condense alcoholMonitoring correctly with prison data`() {
    // Given
    val fieldPosition = mapOf("timeframe" to 0, "endDate" to 1)
    val conditionMetaData = aConditionMetadata(userInput = "alcoholMonitoring", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "timeframe" to "Every morning and evening",
      "endDate" to "2026-03-10",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("timeframe", "Every morning and evening ending on 2026-03-10"),
    )
  }

  @Test
  fun `should condense alcoholMonitoring with missing field correctly with prison data`() {
    // Given
    val fieldPosition = mapOf("timeframe" to 0, "endDate" to 1)
    val conditionMetaData = aConditionMetadata(userInput = "alcoholMonitoring", fieldPosition = fieldPosition)
    val additionalFields = mapOf(
      "endDate" to "2026-03-10",
    )

    // When
    val result = condenser.mergeIfRequired(conditionMetaData, additionalFields)

    // Then
    assertThat(result).containsExactly(
      entry("timeframe", "2026-03-10"),
    )
  }

  fun aConditionMetadata(
    id: String = "default-id",
    text: String = "default text",
    userInput: String = "defaultUserInput",
    fieldPosition: Map<String, Int> = emptyMap(),
    groupName: String = "defaultGroup",
    subgroupName: String? = null,
  ): ConditionMetadata = ConditionMetadata(
    id = id,
    text = text,
    userInput = userInput,
    fieldPosition = fieldPosition,
    groupName = groupName,
    subgroupName = subgroupName,
  )
}
