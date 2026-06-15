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
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.response.LicenceMigrationLogEntryDto
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate
import java.util.concurrent.Executor

class MigrationBatchControllerTest : SqsIntegrationTestBase() {

  private lateinit var cvlMockServer: CvlApiMockServer

  data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
  )

  @Autowired
  private lateinit var migrationRepository: MigrationRepository

  private fun jsonFromFile(name: String): String = this.javaClass.getResourceAsStream("/test_data/migration/$name")!!
    .bufferedReader(UTF_8).readText()

  @TestConfiguration
  class DisableAsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor = SyncTaskExecutor()
  }

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
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
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
    response.expectStatus().isAccepted

    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_1_of_batch.json")
    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_2_of_batch.json")
    verifyRequestPayloadSentToCVL("test_hdc_to_cvl_licence_3_of_batch.json")

    assertThat(migrationRepository.getMigrationLog(1, true, retry = false)).isEqualTo("migrated successfully")
    assertThat(migrationRepository.getMigrationLog(2, true, retry = false)).isEqualTo("migrated successfully")
    assertThat(migrationRepository.getMigrationLog(3, true, retry = false)).isEqualTo("migrated successfully")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-licences-with-unknown_conditions.sql",
  )
  @Test
  fun `When batch has only unknown versioned conditions then do not migrate licence to CVL`() {
    // Given
    val licenceVersionId = 1L
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
      listOf(
        defaultPrisoner(bookingId = "54222", prisonerNumber = "A12345B"),
      ),
    )

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isAccepted
    assertThat(migrationRepository.getMigrationLog(licenceVersionId, false, retry = false)).isEqualTo("Licence additional conditions version not determined!")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-licences-with-different_versions.sql",
  )
  @Test
  fun `When batched licences have multiple versions then latest version is migrated to CVL`() {
    // Given
    val expectedLatestVersionId = 3L
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
      listOf(
        defaultPrisoner(bookingId = "54222", prisonerNumber = "A12345B"),
      ),
    )
    prisonApiMockServer.getHdcStatuses(listOf(11L to "APPROVED"))
    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isAccepted
    assertThat(migrationRepository.getMigrationLogCount()).isEqualTo(1)
    assertThat(migrationRepository.getMigrationLog(expectedLatestVersionId, true, retry = false)).isEqualTo("migrated successfully")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrat-licences_for_second-time.sql",
  )
  @Test
  fun `When licences are already migrated only retriable ones are allowed on second run`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
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
    response.expectStatus().isAccepted

    cvlMockServer.verify(1, postRequestedFor(urlEqualTo("/licences/migrate/active")))

    assertThat(migrationRepository.getMigrationLogCount() - originalLogSize).isEqualTo(1)
    assertThat(migrationRepository.getMigrationLog(2, true, retry = false)).isEqualTo("migrated successfully")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrat-licences_for_second-time.sql",
  )
  @Test
  fun `When licences have a different booking id from the prisoner migration fails and the licence is not migrated`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
      listOf(
        defaultPrisoner(bookingId = "666", prisonerNumber = "A1234EF"),
      ),
    )

    val originalLogSize = migrationRepository.getMigrationLogCount()

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isAccepted

    cvlMockServer.verify(0, postRequestedFor(urlEqualTo("/licences/migrate/active")))
    assertThat(migrationRepository.getMigrationLogCount() - originalLogSize).isEqualTo(1)
    assertThat(migrationRepository.getMigrationLog(2, false, retry = false)).isEqualTo("Old booking id in hdc, 11 != 666 prisoner booking id, status: INACTIVE OUT")
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-batched-licences.sql",
  )
  @Test
  fun `When client exceptions are thrown other licences are still migrated to CVL successfully`() {
    // Given
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
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
    response.expectStatus().isAccepted

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
    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(listOf())

    // When
    val response = postForBatchToMigrate()

    // Then
    response.expectStatus().isAccepted
    cvlMockServer.verify(0, postRequestedFor(urlEqualTo("/licences/migrate/active")))
    assertThat(migrationRepository.getMigrationLog(1, false, retry = false)).isEqualTo("Prisoner not found for prisoner number A1234EE")
    assertThat(migrationRepository.getMigrationLog(2, false, retry = false)).isEqualTo("Prisoner not found for prisoner number B1234EE")
    assertThat(migrationRepository.getMigrationLog(3, false, retry = false)).isEqualTo("Prisoner not found for prisoner number C1234EE")
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
    response.expectStatus().isAccepted
    cvlMockServer.verify(0, postRequestedFor(urlEqualTo("/licences/migrate/active")))
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Get migration logs returns recent log entries`() {
    // Given
    val pageParams = "?page=0&size=5"

    // When
    val response = getMigrationLogs(pageParams)

    // Then
    response.expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)

    val body = response.expectBody<PageResponse<LicenceMigrationLogEntryDto>>()
      .returnResult()
      .responseBody

    val logs = body!!.content

    assertThat(logs).hasSize(3)
    assertThat(logs).containsExactly(
      LicenceMigrationLogEntryDto(id = 3, licenceVersionId = 3, bookingId = 30, success = false, retry = false, message = "Prisoner not found for prisoner number C1234EE", errorSource = "HDC"),
      LicenceMigrationLogEntryDto(id = 2, licenceVersionId = 2, bookingId = 20, success = false, retry = true, message = "Service has failed - retry", errorSource = "CVL"),
      LicenceMigrationLogEntryDto(id = 1, licenceVersionId = 1, bookingId = 10, success = true, retry = false, message = "migrated successfully", errorSource = null),
    )
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Get next page migration logs returns recent log entries`() {
    // Given
    val pageParams = "?page=1&size=1"

    // When
    val response = getMigrationLogs(pageParams)

    // Then
    response.expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)

    val body = response.expectBody<PageResponse<LicenceMigrationLogEntryDto>>()
      .returnResult()
      .responseBody

    val logs = body!!.content

    assertThat(logs).hasSize(1)
    assertThat(logs).containsExactly(
      LicenceMigrationLogEntryDto(id = 2, licenceVersionId = 2, bookingId = 20, success = false, retry = true, message = "Service has failed - retry", errorSource = "CVL"),
    )
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Get migration logs filtered by licence version id`() {
    // Given
    val licenceVersionId = 2L

    // When
    val response = getMigrationLogs("?licenceVersionId=$licenceVersionId")

    // Then
    response.expectStatus().isOk
      .expectBody<PageResponse<LicenceMigrationLogEntryDto>>()
      .consumeWith {
        val body = it.responseBody
        assertThat(body!!.content).hasSize(1)
        assertThat(body.content[0].licenceVersionId).isEqualTo(licenceVersionId)
      }
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Get migration logs filtered by booking id`() {
    // Given
    val bookingId = 30L

    // When
    val response = getMigrationLogs("?bookingId=$bookingId")

    // Then
    response.expectStatus().isOk
      .expectBody<PageResponse<LicenceMigrationLogEntryDto>>()
      .consumeWith {
        val body = it.responseBody!!
        assertThat(body.content).hasSize(1)
        assertThat(body.content[0].bookingId).isEqualTo(bookingId)
      }
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Get migration logs filtered by error source`() {
    // Given
    val errorSource = "CVL"

    // When
    val response = getMigrationLogs("?errorSource=$errorSource")

    // Then
    response.expectStatus().isOk
      .expectBody<PageResponse<LicenceMigrationLogEntryDto>>()
      .consumeWith {
        val body = it.responseBody!!
        assertThat(body.content).hasSize(1)
        assertThat(body.content[0].errorSource).isEqualTo(errorSource)
      }
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Get migration logs filtered by multiple parameters`() {
    // Given
    val multiBookingId = 10L
    val multiErrorSource = "HDC"

    // When
    val multiResponse = getMigrationLogs("?bookingId=$multiBookingId&errorSource=$multiErrorSource")

    // Then
    multiResponse.expectStatus().isOk
      .expectBody<PageResponse<LicenceMigrationLogEntryDto>>()
      .consumeWith {
        val body = it.responseBody!!
        assertThat(body.content).isEmpty()
      }
  }

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migration-logs.sql",
  )
  @Test
  fun `Update retry flag for a licence version`() {
    // Given
    val licenceVersionId = 1L
    val retryValue = true

    // When
    webTestClient.put()
      .uri("/licences/migrate/$licenceVersionId/retry/$retryValue")
      .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent

    // Then
    assertThat(migrationRepository.getMigrationLog(licenceVersionId, true, retry = true)).isEqualTo("migrated successfully")
  }

  private fun postForBatchToMigrate(): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/licences/migrate/batch/to-cvl")
    .headers(setAuthorisation(roles = listOf("ROLE_HDC_ADMIN")))
    .accept(MediaType.APPLICATION_JSON)
    .exchange()

  private fun getMigrationLogs(queryParams: String = ""): WebTestClient.ResponseSpec = webTestClient.get()
    .uri("/licences/migrate/logs$queryParams")
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
    prisonId = "AWE",
    lastPrisonId = "MDI",
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
