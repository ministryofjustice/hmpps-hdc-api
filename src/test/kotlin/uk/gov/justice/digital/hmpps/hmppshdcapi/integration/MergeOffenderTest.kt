package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.AdditionalInformationMerge
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.HMPPSMergeDomainEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.LicenceVersionRepository
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.MERGE_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.PrisonOffenderEventListener.Companion.PRISONER_MERGE_EVENT_TYPE
import java.time.Duration
import java.time.Instant

private const val OLD_PRISON_NUMBER = "A1234AA"
private const val NEW_PRISON_NUMBER = "B1234BB"

class MergeOffenderTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var licenceRepository: LicenceRepository

  @Autowired
  lateinit var licenceVersionRepository: LicenceVersionRepository

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/merge-offender.sql",
  )
  fun check() {
    assertThat(licenceRepository.findAllByPrisonNumber(OLD_PRISON_NUMBER)).hasSize(3)
    assertThat(licenceRepository.findAllByPrisonNumber(NEW_PRISON_NUMBER)).hasSize(1)
    assertThat(licenceVersionRepository.findAllByPrisonNumber(OLD_PRISON_NUMBER)).hasSize(1)
    assertThat(licenceVersionRepository.findAllByPrisonNumber(NEW_PRISON_NUMBER)).hasSize(1)

    publishDomainEventMessage(
      PRISONER_MERGE_EVENT_TYPE,
      AdditionalInformationMerge(removedNomsNumber = OLD_PRISON_NUMBER, nomsNumber = NEW_PRISON_NUMBER),
      "A prisoner has been merged from $OLD_PRISON_NUMBER to $NEW_PRISON_NUMBER",
    )

    awaitAtMost30Secs untilAsserted {
      verify(eventProcessingComplete).complete()
    }

    verify(telemetryClient).trackEvent(
      MERGE_EVENT_NAME,
      mapOf(
        "NOMS-MERGE-FROM" to OLD_PRISON_NUMBER,
        "NOMS-MERGE-TO" to NEW_PRISON_NUMBER,
        "UPDATED-LICENCE-RECORDS" to "3",
        "UPDATED-LICENCE-VERSION-RECORDS" to "1",
      ),
      null,
    )

    assertThat(licenceRepository.findAllByPrisonNumber(OLD_PRISON_NUMBER)).hasSize(0)
    assertThat(licenceRepository.findAllByPrisonNumber(NEW_PRISON_NUMBER)).hasSize(4)
    assertThat(licenceVersionRepository.findAllByPrisonNumber(OLD_PRISON_NUMBER)).hasSize(0)
    assertThat(licenceVersionRepository.findAllByPrisonNumber(NEW_PRISON_NUMBER)).hasSize(2)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/merge-offender.sql",
  )
  fun checkNoEventWhenNoRecordsToUpdate() {
    val someNonExistentPrisonNumber = "ZZ1234AA"
    assertThat(licenceRepository.findAllByPrisonNumber(someNonExistentPrisonNumber)).hasSize(0)
    assertThat(licenceRepository.findAllByPrisonNumber(someNonExistentPrisonNumber)).hasSize(0)

    publishDomainEventMessage(
      PRISONER_MERGE_EVENT_TYPE,
      AdditionalInformationMerge(removedNomsNumber = someNonExistentPrisonNumber, nomsNumber = NEW_PRISON_NUMBER),
      "A prisoner has been merged from $someNonExistentPrisonNumber to $NEW_PRISON_NUMBER",
    )

    awaitAtMost30Secs untilAsserted {
      verify(eventProcessingComplete).complete()
    }

    verifyNoInteractions(telemetryClient)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  private fun publishDomainEventMessage(
    eventType: String,
    additionalInformation: AdditionalInformationMerge,
    description: String,
  ) {
    domainEventsTopicSnsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopicArn)
        .message(
          jsonString(
            HMPPSMergeDomainEvent(
              eventType = eventType,
              additionalInformation = additionalInformation,
              occurredAt = Instant.now().toString(),
              description = description,
              version = "1.0",
            ),
          ),
        )
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        )
        .build(),
    )
  }
}
