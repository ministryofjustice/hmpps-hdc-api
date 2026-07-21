package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock

import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension

class DeliusMockServer :
  WireMockExtension(
    extensionOptions()
      .options(wireMockConfig().port(8093)),
  ) {

  private val mapper: com.fasterxml.jackson.databind.ObjectMapper = JsonMapper.builder().findAndAddModules().build()

  fun stubGetOffenderManagerWithNomsId(
    nomsId: String = "A1234AA",
    userName: String = "AZ12345",
    staffCode: String = "staff-1",
    emailAddress: String = "user@test.com",
    staffIdentifier: Long = 125,
    firstName: String = "firstName",
    lastName: String = "lastName",
  ) {
    stubFor(
      get(urlEqualTo("/probation-case/$nomsId/responsible-community-manager")).willReturn(
        aResponse().withHeader("Content-Type", "application/json").withBody(
          // language=json
          """{
            "code": "$staffCode", 
            "id": $staffIdentifier,
            "case": { "crn": "X12345", "nomisId": "$nomsId" },
            "name": { "forename": "$firstName", "surname": "$lastName" },
            "allocationDate": "2022-01-02",
            "team": {
              "code": "team-code-1",
              "description": "staff-description-1",
              "borough": { "code": "borough-code-1", "description": "borough-description-1" },
              "district": { "code": "district-code-1", "description": "district-description-1" },
              "provider": { "code": "probationArea-code-1", "description": "probationArea-description-1" }
            },
            "provider": { 
              "code": "probationArea-code-1", 
              "description": "probationArea-description-1"
            },
            "email": "$emailAddress",
            "username" : "$userName"
          }""",
        ).withStatus(200),
      ),
    )
  }
}
