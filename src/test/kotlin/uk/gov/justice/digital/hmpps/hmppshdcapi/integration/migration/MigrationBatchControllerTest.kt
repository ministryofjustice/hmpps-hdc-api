package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.migration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate

class MigrationBatchControllerTest : SqsIntegrationTestBase() {

  private lateinit var cvlMockServer: CvlApiMockServer

  @Autowired
  private lateinit var migrationRepository: MigrationRepository

  private fun jsonFromFile(name: String): String = this.javaClass.getResourceAsStream("/test_data/migration/$name")!!
    .bufferedReader(UTF_8).readText()

  @BeforeEach
  fun resetMocks() {
    cvlMockServer = CvlApiMockServer().apply { start() }
  }

  @AfterEach
  fun tearDown() {
    cvlMockServer.stop()
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-batched-licences.sql",
  )
  @Test
  fun `Migrate a batch of licences to CVL successfully`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
      listOf(
        defaultPrisoner(bookingId = "10", prisonerNumber = "A1234EE"),
        defaultPrisoner(bookingId = "20", prisonerNumber = "B1234EE"),
        defaultPrisoner(bookingId = "30", prisonerNumber = "C1234EE"),
      ),
    )

    prisonApiMockServer.getHdcStatuses(listOf(10L to "APPROVED", 20L to "APPROVED", 30L to "APPROVED"))
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isOk

    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_1_of_batch.json")
    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_2_of_batch.json")
    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_3_of_batch.json")

    assertThat(migrationRepository.getMigrationLog(1, true, retry = false)).isEqualTo("migrated successfully")
    assertThat(migrationRepository.getMigrationLog(2, true, retry = false)).isEqualTo("migrated successfully")
    assertThat(migrationRepository.getMigrationLog(3, true, retry = false)).isEqualTo("migrated successfully")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrat-licences_for_second-time.sql",
  )
  @Test
  fun `When licences are already migrated only retriable ones are allowed on second run`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
      listOf(
        defaultPrisoner(bookingId = "11", prisonerNumber = "A1234EF"),
      ),
    )

    prisonApiMockServer.getHdcStatuses(listOf(11L to "APPROVED"))
    cvlMockServer.stubMigrateLicenceSuccess()
    val originalLogSize = migrationRepository.getMigrationLogCount()

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isOk

    cvlMockServer.verify(1, postRequestedFor(urlEqualTo("/licences/migrate/active")))

    assertThat(migrationRepository.getMigrationLogCount() - originalLogSize).isEqualTo(1)
    assertThat(migrationRepository.getMigrationLog(2, true, retry = false)).isEqualTo("migrated successfully")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-batched-licences.sql",
  )
  @Test
  fun `When client exceptions are thrown other licences are still migrated to CVL successfully`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(
      listOf(
        defaultPrisoner(bookingId = "10", prisonerNumber = "A1234EE"),
        defaultPrisoner(bookingId = "20", prisonerNumber = "B1234EE"),
        defaultPrisoner(bookingId = "30", prisonerNumber = "C1234EE"),
      ),
    )

    prisonApiMockServer.getHdcStatuses(listOf(10L to "APPROVED", 20L to "APPROVED", 30L to "APPROVED"))
    cvlMockServer.stubMigrateLicenceClient500Then200Then405()

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isOk

    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_2_of_batch.json")
    assertThat(migrationRepository.getMigrationLog(1, false, retry = true)).isEqualTo("Service has failed - retry")
    assertThat(migrationRepository.getMigrationLog(2, true, retry = false)).isEqualTo("migrated successfully")
    assertThat(migrationRepository.getMigrationLog(3, false, retry = false)).isEqualTo("Final failure - dont retry")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-batched-licences.sql",
  )
  @Test
  fun `Do not migrate a batch of licences when prisoners cannot be found`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByBookingIds(listOf())

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isOk
    cvlMockServer.verify(0, postRequestedFor(urlEqualTo("/licences/migrate/active")))
    assertThat(migrationRepository.getMigrationLog(1, false, retry = false)).isEqualTo("Prisoner not found for booking id 10")
    assertThat(migrationRepository.getMigrationLog(2, false, retry = false)).isEqualTo("Prisoner not found for booking id 20")
    assertThat(migrationRepository.getMigrationLog(3, false, retry = false)).isEqualTo("Prisoner not found for booking id 30")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-invalid-licences.sql",
  )
  @Test
  fun `When SQL finds no valid licences to migrate, no licences are migrated to CVL`() {
    // Given
    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isOk
    cvlMockServer.verify(0, postRequestedFor(urlEqualTo("/licences/migrate/active")))
  }

  private fun postForBatchToMigrate(): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/licences/migrate/active/batch/to-cvl")
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

  fun defaultPrisoner(
    prisonerNumber: String = "A1234AA",
    bookingId: String = "10",
    restrictedPatient: Boolean = false,
    middleNames: String? = "middleNames",
    firstName: String = "forename",
    lastName: String = "surname",
    dateOfBirth: LocalDate = LocalDate.of(1985, 5, 20),
  ) = Prisoner(
    prisonerNumber = prisonerNumber,
    bookingId = bookingId,
    prisonId = "MDI",
    topupSupervisionExpiryDate = LocalDate.of(2028, 2, 10),
    licenceExpiryDate = LocalDate.of(2028, 3, 30),
    homeDetentionCurfewActualDate = LocalDate.of(2025, 4, 30),
    homeDetentionCurfewEligibilityDate = LocalDate.of(2025, 3, 20),
    pncNumber = "PNC123",
    status = "INACTIVE OUT",
    mostSeriousOffence = "Theft",
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
    firstName = firstName,
    middleNames = middleNames,
    lastName = lastName,
    dateOfBirth = dateOfBirth,
    conditionalReleaseDateOverrideDate = LocalDate.of(2025, 4, 7),
    sentenceStartDate = LocalDate.of(2020, 1, 1),
    sentenceExpiryDate = LocalDate.of(2025, 1, 1),
    topupSupervisionStartDate = LocalDate.of(2023, 1, 1),
    croNumber = "CRO123",
    restrictedPatient = restrictedPatient,
  )

  companion object {

    private val prisonerSearchMockServer = PrisonerSearchMockServer()
    private val prisonApiMockServer = PrisonApiMockServer()

    @JvmStatic
    @BeforeAll
    fun startWireMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonerSearchMockServer.start()
      prisonApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopWireMocks() {
      hmppsAuthMockServer.stop()
      prisonerSearchMockServer.stop()
      prisonApiMockServer.stop()
    }
  }
}
