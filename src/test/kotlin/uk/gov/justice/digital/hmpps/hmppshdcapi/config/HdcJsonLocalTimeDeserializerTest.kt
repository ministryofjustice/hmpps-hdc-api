import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppshdcapi.config.HdcJsonLocalTimeDeserializer
import java.time.LocalTime

class HdcJsonLocalTimeDeserializerTest {

  data class TestWrapper(
    val time: LocalTime?,
  )

  private val mapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(
      SimpleModule().addDeserializer(
        LocalTime::class.java,
        HdcJsonLocalTimeDeserializer(),
      ),
    )

  @Test
  fun `should deserialize valid time`() {
    // Given
    val json = """{"time":"10:15:30"}"""

    // When
    val result: TestWrapper = mapper.readValue(json, TestWrapper::class.java)

    // Then
    assertThat(result.time).isEqualTo(LocalTime.of(10, 15, 30))
  }

  @Test
  fun `should return null for blank string`() {
    // Given
    val json = """{"time":""}"""

    // When
    val result = mapper.readValue(json, TestWrapper::class.java)

    // Then
    assertThat(result.time).isNull()
  }

  @Test
  fun `should return null for null value`() {
    // Given
    val json = """{"time":null}"""

    // When
    val result = mapper.readValue(json, TestWrapper::class.java)

    // Then
    assertThat(result.time).isNull()
  }

  @Test
  fun `should return null for invalid time`() {
    // Given
    val json = """{"time":"not-a-time"}"""

    // When
    val result = mapper.readValue(json, TestWrapper::class.java)

    // Then
    assertThat(result.time).isNull()
  }
}
