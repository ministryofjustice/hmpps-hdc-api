package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.migration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode.STRICT
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate

class MigrationControllerTest : SqsIntegrationTestBase() {

  private fun jsonFromFile(name: String): String = this.javaClass.getResourceAsStream("/test_data/migration/$name")!!
    .bufferedReader(UTF_8).readText()

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/hdc-migrated-licences.sql",
  )
  @Test
  fun `Migrate licence to CVL successfully`() {
    val licenceId = 1
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()

    // Given
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = webTestClient.post()
      .uri("/licences/migration/$licenceId/to-cvl")
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()

    // Then
    response.expectStatus().isOk

    // Verify the request payload sent to CVL
    cvlMockServer.verify(
      1,
      postRequestedFor(urlEqualTo("/licences/migrate"))
        .withRequestBody(
          equalToJson(
            jsonFromFile("migration_test_1.json"),
            true,
            false,
          ),
        ),
    )
  }

  @Test
  fun `Preview migration returns expected DTO`() {
    val licenceId = 1

    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()

    // When
    val response = webTestClient.post()
      .uri("/licences/migration/$licenceId/to-cvl/preview")
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()

    // Then
    response.expectStatus().isOk
      .expectBody()
      .json(
        jsonFromFile("migration_test_1.json"),
        STRICT,
      )
  }

  fun stubGetHdcStatuses() {
    prisonApiMockServer.getHdcStatuses(listOf(12345L to "APPROVED", 54321L to "OTHER", 98765L to "REJECTED"))
  }

  fun stubSearchPrisonersByBookingIds() = prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
    listOf(
      Prisoner(
        prisonerNumber = "A1234AA",
        bookingId = "54222",
        prisonId = "MDI",
        topupSupervisionExpiryDate = LocalDate.of(2025, 4, 1),
        licenceExpiryDate = LocalDate.of(2025, 3, 31),
        homeDetentionCurfewEligibilityDate = LocalDate.of(2025, 3, 30),
        pncNumber = "PNC123",
        status = "ACTIVE",
        mostSeriousOffence = "Theft",
        homeDetentionCurfewActualDate = LocalDate.of(2025, 3, 25),
        homeDetentionCurfewEndDate = LocalDate.of(2025, 4, 11),
        releaseDate = LocalDate.of(2025, 4, 16),
        confirmedReleaseDate = LocalDate.of(2025, 4, 17),
        conditionalReleaseDate = LocalDate.of(2025, 4, 12),
        paroleEligibilityDate = LocalDate.of(2025, 4, 8),
        actualParoleDate = LocalDate.of(2025, 4, 9),
        releaseOnTemporaryLicenceDate = LocalDate.of(2025, 4, 6),
        postRecallReleaseDate = LocalDate.of(2025, 4, 21),
        legalStatus = "SENTENCED",
        indeterminateSentence = false,
        imprisonmentStatus = "IMPRISONED",
        imprisonmentStatusDescription = "Serving sentence",
        recall = false,
        locationDescription = "Cell 12A",
        prisonName = "Manchester Prison",
        bookNumber = "64321",
        firstName = "forename",
        middleNames = "middleNames",
        lastName = "surname",
        dateOfBirth = LocalDate.of(1985, 5, 20),
        conditionalReleaseDateOverrideDate = LocalDate.of(2025, 4, 7),
        sentenceStartDate = LocalDate.of(2020, 1, 1),
        sentenceExpiryDate = LocalDate.of(2025, 1, 1),
        topupSupervisionStartDate = LocalDate.of(2023, 1, 1),
        croNumber = "CRO123",
      ),
      Prisoner(
        prisonerNumber = "A1234CC",
        bookingId = "30",
        prisonId = "MDI",
        topupSupervisionExpiryDate = null,
        licenceExpiryDate = LocalDate.of(2025, 4, 1),
        homeDetentionCurfewEligibilityDate = LocalDate.of(2025, 3, 30),
        pncNumber = "PNC456",
        status = "INACTIVE",
        mostSeriousOffence = "Assault",
        homeDetentionCurfewActualDate = null,
        homeDetentionCurfewEndDate = null,
        releaseDate = LocalDate.of(2025, 4, 11),
        confirmedReleaseDate = null,
        conditionalReleaseDate = LocalDate.of(2025, 4, 9),
        paroleEligibilityDate = LocalDate.of(2025, 4, 6),
        actualParoleDate = null,
        releaseOnTemporaryLicenceDate = null,
        postRecallReleaseDate = LocalDate.of(2025, 4, 13),
        legalStatus = "REMAND",
        indeterminateSentence = true,
        imprisonmentStatus = "ON_HDC",
        imprisonmentStatusDescription = "Home detention curfew",
        recall = true,
        locationDescription = "Cell 5B",
        prisonName = "Manchester Prison",
        bookNumber = "54322",
        firstName = "Jane",
        middleNames = "A",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1990, 3, 15),
        conditionalReleaseDateOverrideDate = null,
        sentenceStartDate = LocalDate.of(2022, 6, 1),
        sentenceExpiryDate = LocalDate.of(2027, 6, 1),
        topupSupervisionStartDate = null,
        croNumber = "CRO456",
      ),
      Prisoner(
        prisonerNumber = "A1234EE",
        bookingId = "50",
        prisonId = "MDI",
        topupSupervisionExpiryDate = null,
        licenceExpiryDate = null,
        homeDetentionCurfewEligibilityDate = LocalDate.of(2025, 3, 30),
        pncNumber = "PNC789",
        status = "ACTIVE",
        mostSeriousOffence = "Fraud",
        homeDetentionCurfewActualDate = LocalDate.of(2025, 3, 31),
        homeDetentionCurfewEndDate = LocalDate.of(2025, 4, 7),
        releaseDate = LocalDate.of(2025, 4, 14),
        confirmedReleaseDate = LocalDate.of(2025, 4, 15),
        conditionalReleaseDate = LocalDate.of(2025, 4, 10),
        paroleEligibilityDate = LocalDate.of(2025, 4, 6),
        actualParoleDate = LocalDate.of(2025, 4, 7),
        releaseOnTemporaryLicenceDate = LocalDate.of(2025, 4, 4),
        postRecallReleaseDate = LocalDate.of(2025, 4, 18),
        legalStatus = "SENTENCED",
        indeterminateSentence = false,
        imprisonmentStatus = "IMPRISONED",
        imprisonmentStatusDescription = "Serving sentence",
        recall = false,
        locationDescription = "Cell 7C",
        prisonName = "Manchester Prison",
        bookNumber = "54323",
        firstName = "Bob",
        middleNames = null,
        lastName = "Brown",
        dateOfBirth = LocalDate.of(1978, 12, 5),
        conditionalReleaseDateOverrideDate = LocalDate.of(2025, 4, 5),
        sentenceStartDate = LocalDate.of(2019, 8, 1),
        sentenceExpiryDate = LocalDate.of(2024, 8, 1),
        topupSupervisionStartDate = null,
        croNumber = "CRO789",
      ),
    ),
  )

  companion object {
    private val cvlMockServer = CvlApiMockServer()
    private val prisonerSearchMockServer = PrisonerSearchMockServer()
    private val prisonApiMockServer = PrisonApiMockServer()

    @JvmStatic
    @BeforeAll
    fun startWireMocks() {
      cvlMockServer.start()
      prisonerSearchMockServer.start()
      prisonApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopWireMocks() {
      cvlMockServer.stop()
      prisonerSearchMockServer.stop()
      prisonApiMockServer.stop()
    }
  }
}
