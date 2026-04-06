package uk.gov.justice.digital.hmpps.hmppshdcapi.model.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SarLicenceDataTest {

  @Test
  fun `extractLastname should return lastname from firstname lastname format`() {
    assertThat(extractLastname("John Smith")).isEqualTo("Smith")
    assertThat(extractLastname("Anne Approver")).isEqualTo("Approver")
    assertThat(extractLastname("Test Client")).isEqualTo("Client")
  }

  @Test
  fun `extractLastname should handle multiple spaces and return last word`() {
    assertThat(extractLastname("John Middle Smith")).isEqualTo("Smith")
    assertThat(extractLastname("Mary Jane Watson")).isEqualTo("Watson")
  }

  @Test
  fun `extractLastname should return original name if no space`() {
    assertThat(extractLastname("SingleName")).isNull()
  }

  @Test
  fun `extractLastname should handle null and blank strings`() {
    assertThat(extractLastname(null)).isNull()
    assertThat(extractLastname("")).isEqualTo("")
    assertThat(extractLastname("   ")).isEqualTo("   ")
  }

  @Test
  fun `extractLastname should handle names with leading and trailing spaces`() {
    assertThat(extractLastname("  John Smith  ")).isEqualTo("Smith")
  }
}
