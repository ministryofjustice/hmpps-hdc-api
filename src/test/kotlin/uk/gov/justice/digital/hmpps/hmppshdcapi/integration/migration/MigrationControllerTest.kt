package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.migration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode.LENIENT
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate

class MigrationControllerTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var migrationRepository: MigrationRepository

  private fun jsonFromFile(name: String): String = this.javaClass.getResourceAsStream("/test_data/migration/$name")!!
    .bufferedReader(UTF_8).readText()

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences.sql",
  )
  @Test
  fun `Migrate licence to CVL successfully`() {
    // Given
    val licenceId = 1L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()

    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_hdc_to_cvl.json")
    assertThat(migrationRepository.migrationLogExists(licenceId)).isTrue
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrate-licence-with-multiple-licences-with-the-same-booking-id-present-in-audit.sql",
  )
  @Test
  fun `Migrate correct audit data when multiple licences with the same booking id present in audit`() {
    // Given
    val licenceId = 1L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_when_multiple-licences-with-same-booking-id-present-in-audit.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrate-specific-curfew-days-for-licence-to-cvl-successfully.sql",
  )
  @Test
  fun `Migrate a licence with day specific inputs for curfew times to CVL successfully`() {
    // Given
    val licenceId = 1L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_with_specific_curfew_days.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences-with-curfew-address.sql",
  )
  @Test
  fun `Migrate correct curfew address when approved premises is required over proposed address`() {
    // Given
    val licenceId = 1L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_when_approved_premises_is_required_over_proposed_address.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences-with-curfew-address.sql",
  )
  @Test
  fun `Migrate correct curfew address when approved premises is required over CAS2 address`() {
    // Given
    val licenceId = 2L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_approved_premises_is_required_over_CAS2_address.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences-with-curfew-address.sql",
  )
  @Test
  fun `Migrate correct curfew address when CAS2 address requested and accepted`() {
    // Given
    val licenceId = 4L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_address_when_CAS2_address_requested_and_accepted.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences-with-curfew-address.sql",
  )
  @Test
  fun `Migrate correct curfew address when curfew address proposed`() {
    // Given
    val licenceId = 5L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_when_no_other_address_is_available_then_use_proposed_curfew_address.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences-with-curfew-address.sql",
  )
  @Test
  fun `Migrate Exception is thrown when no address can be found`() {
    // Given
    val licenceId = 3L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isBadRequest
    assertThat(migrationRepository.migrationLogExists(licenceId)).isFalse
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences.sql",
  )
  @Test
  fun `Migrate appointment details when appointment time and date not given the process correctly`() {
    // Given
    val licenceId = 6L
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postLicenceIdToMigrate(licenceId)

    // Then
    response.expectStatus().isOk
    verifyRequestPayloadSentToCVL("test_when_no_appointment_date_time_given.json")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences.sql",
  )
  @Test
  fun `Preview migration returns expected DTO`() {
    // Given
    val licenceId = 1
    stubSearchPrisonersByBookingIds()
    stubGetHdcStatuses()

    // When
    val response = webTestClient.get()
      .uri("/licences/migrate/active/$licenceId/to-cvl/preview")
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()

    // Then
    response.expectStatus().isOk
      .expectBody()
      .json(
        jsonFromFile("test_hdc_to_cvl.json"),
        LENIENT,
      )
  }

  private fun postLicenceIdToMigrate(licenceId: Long): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/licences/migrate/active/$licenceId/to-cvl")
    .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
    .accept(MediaType.APPLICATION_JSON)
    .exchange()

  private fun verifyRequestPayloadSentToCVL(testUri: String) {
    cvlMockServer.verify(
      1,
      postRequestedFor(urlEqualTo("/licences/migrate/active"))
        .withRequestBody(
          equalToJson(
            jsonFromFile(testUri),
            true,
            false,
          ),
        ),
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
