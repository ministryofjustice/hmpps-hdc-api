package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.charset.StandardCharsets.UTF_8

class MarshallingTest {
  private val objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().addModule(JavaTimeModule()).build()

  private fun jsonFromFile(name: String) = this.javaClass.getResourceAsStream("/test_data/condition_mapping/$name")!!.bufferedReader(UTF_8).readText()

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
  )
  fun `check licence serialization for {0}`(fileName: String) {
    val contents = jsonFromFile(fileName)
    val tree = objectMapper.readTree(contents)
    val licenceData = objectMapper.convertValue(tree, LicenceData::class.java)

    val licenceDataAsMap = objectMapper.convertValue(tree, Map::class.java)
    val licenceDataToMap = objectMapper.convertValue(licenceData, Map::class.java)

    assertThat(licenceDataAsMap).isEqualTo(licenceDataToMap)
  }
}
