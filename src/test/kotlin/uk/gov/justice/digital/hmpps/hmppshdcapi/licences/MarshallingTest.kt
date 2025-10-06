package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.nio.charset.StandardCharsets.UTF_8
import java.util.stream.Stream

class MarshallingTest {
  private val objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().addModule(JavaTimeModule()).build()

  val ignoredFields = mapOf(
    // Version is added automatically when not present, which makes the deserialized and non-deserialized mismatch
    "risk_management_v1-c.json" to listOf("risk.riskManagement.version"),
  )

  @ParameterizedTest
  @CsvSource(
    // unsuitable
    "eligibility-unsuitability.json",

    // abuseAndBehaviours is array
    "full_conditions_v1.json",
    // abuseAndBehaviours is string
    "full_conditions_v1-b.json",
    // abuseAndBehaviours is array
    "full_conditions_v2.json",
    // abuseAndBehaviours is string
    "full_conditions_v2-b.json",

    // A few but not all conditions
    "full_conditions_v1-partial.json",

    // With EMS info
    "risk_management_v1.json",
    // Without EMS info
    "risk_management_v1-b.json",
    // No version
    "risk_management_v1-c.json",
    // With EMS info
    "risk_management_v2.json",
    // Without EMS info
    "risk_management_v2-b.json",
    // With EMS info
    "risk_management_v3.json",
    // Without EMS info
    "risk_management_v3-b.json",

    // Main occupier = false
    "address_review_v1.json",
    // Main occupier = true
    "address_review_v1-b.json",
    // Main occupier = false
    "address_review_v2.json",
    // Main occupier = true
    "address_review_v2-b.json",

    // single approval reason
    "release-v1.json",
    // multiple approval reasons
    "release-v2.json",

    // bass with rejection
    "bass.json",

    // approve
    "approve_premises.json",
    // approval with final checks
    "approve_premises_v2.json",
  )
  fun `check licence serialization for {0}`(fileName: String) {
    val contents = jsonFromFile(fileName)
    val tree = objectMapper.readTree(contents)
    val licenceData = objectMapper.convertValue(tree, LicenceData::class.java)

    val licenceDataAsMap = objectMapper.convertValue(tree, Map::class.java)
    val licenceDataToMap = objectMapper.convertValue(licenceData, Map::class.java)

    assertThat(licenceDataAsMap)
      .usingRecursiveComparison()
      .ignoringFields(*(ignoredFields[fileName] ?: emptyList()).toTypedArray())
      .isEqualTo(licenceDataToMap)
  }

  @ParameterizedTest(name = "Case {index}")
  @MethodSource("jsonLines")
  fun `check others`(line: String) {
    val tree = objectMapper.readTree(line)
    val licenceData = objectMapper.convertValue(tree, LicenceData::class.java)

    val licenceDataAsMap = objectMapper.convertValue(tree, Map::class.java)
    val licenceDataToMap = objectMapper.convertValue(licenceData, Map::class.java)

    assertThat(licenceDataAsMap)
      .usingRecursiveComparison()
      .isEqualTo(licenceDataToMap)
  }

  companion object {
    private fun jsonFromFile(name: String) = MarshallingTest::class.java.getResourceAsStream("/test_data/condition_mapping/$name")!!.bufferedReader(UTF_8)
      .readText()

    @JvmStatic
    fun jsonLines(): Stream<String> = jsonFromFile("others.jsonl").split("\n").filter { it.isNotBlank() }.stream()
  }
}
