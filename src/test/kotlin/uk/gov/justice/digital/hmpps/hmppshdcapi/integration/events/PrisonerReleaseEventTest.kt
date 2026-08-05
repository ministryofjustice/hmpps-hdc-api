package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.events

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonApiMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.PRISONER_RELEASED_EVENT
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HMPPSPrisonerUpdateEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HMPPSPrisonerUpdatedAdditionalInformation
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.repository.MigrationRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.prison.Prisoner
import java.time.Duration
import java.time.LocalDate

class PrisonerReleaseEventTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var migrationRepository: MigrationRepository

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/migration/sql/hdc-migrated-licences.sql",
  )
  fun `When release event is sent for licence and prisoner has correct criteria then migrates to CVL successfully`() {
    // Given
    val prisonNumber = "A12345B"
    val reason = "RELEASED"

    prisonerSearchMockServer.stubSearchPrisonersByPrisonerNumbers(
      listOf(
        defaultPrisoner(
          bookingId = "54222",
          prisonerNumber = "A1234EE",
          homeDetentionCurfewActualDate = LocalDate.now(),
          conditionalReleaseDate = LocalDate.now().plusDays(10),
        ),
      ),
    )

    cvlMockServer.stubMigrateLicenceSuccess()

    // When
    publishDomainEventMessage(
      HMPPSPrisonerUpdatedAdditionalInformation(nomsNumber = prisonNumber, reason = reason),
    )

    awaitAtMost30Secs untilAsserted {
      verify(eventProcessingComplete).complete()
    }

    // Then
    assertThat(migrationRepository.getMigrationLog(1L, true, retry = false)).isEqualTo("migrated successfully")
    assertThat(migrationRepository.findMigrationStateById(1L)).isEqualTo("COMPLETED")
  }

  private fun publishDomainEventMessage(
    additionalInformation: HMPPSPrisonerUpdatedAdditionalInformation,
  ) {
    domainEventsTopicSnsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopicArn)
        .message(
          jsonString(
            HMPPSPrisonerUpdateEvent(
              additionalInformation = additionalInformation,
            ),
          ),
        )
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(PRISONER_RELEASED_EVENT).build(),
          ),
        )
        .build(),
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
    homeDetentionCurfewActualDate: LocalDate? = null,
    conditionalReleaseDate: LocalDate? = null,
  ) = Prisoner(
    prisonerNumber = prisonerNumber,
    bookingId = bookingId,
    prisonId = "AWE",
    lastPrisonId = "MDI",
    topupSupervisionExpiryDate = LocalDate.of(2028, 2, 10),
    licenceExpiryDate = LocalDate.of(2028, 3, 30),
    homeDetentionCurfewActualDate = homeDetentionCurfewActualDate,
    homeDetentionCurfewEligibilityDate = LocalDate.of(2025, 3, 20),
    pncNumber = "PNC123",
    status = "INACTIVE OUT",
    mostSeriousOffence = "Theft",
    homeDetentionCurfewEndDate = LocalDate.of(2025, 4, 30),
    releaseDate = LocalDate.of(2025, 4, 16),
    confirmedReleaseDate = LocalDate.of(2025, 4, 12),
    conditionalReleaseDate = conditionalReleaseDate,
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
    private lateinit var cvlMockServer: CvlApiMockServer

    @JvmStatic
    @BeforeAll
    fun startWireMocks() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
      prisonerSearchMockServer.start()
      prisonApiMockServer.start()
      cvlMockServer = CvlApiMockServer().apply { start() }
    }

    @JvmStatic
    @AfterAll
    fun stopWireMocks() {
      hmppsAuthMockServer.stop()
      prisonerSearchMockServer.stop()
      prisonApiMockServer.stop()
      cvlMockServer.stop()
    }
  }
}
