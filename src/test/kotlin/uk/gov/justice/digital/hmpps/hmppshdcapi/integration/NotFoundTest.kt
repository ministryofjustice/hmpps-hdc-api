package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.MIGRATION_ROLE

class NotFoundTest : SqsIntegrationTestBase() {

  @Test
  fun `Resources that aren't found should return 404 - test of the exception handler`() {
    webTestClient.get().uri("/some-url-not-found")
      .headers(setAuthorisation(roles = listOf("ROLE_$MIGRATION_ROLE")))
      .exchange()
      .expectStatus().isNotFound
  }
}
