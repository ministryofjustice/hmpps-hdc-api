package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase

class InitialiseDatabaseTest : SqsIntegrationTestBase() {

  @Test
  fun `initialises database`() {
    println("Database has been initialised by SqsIntegrationTestBase")
  }
}
