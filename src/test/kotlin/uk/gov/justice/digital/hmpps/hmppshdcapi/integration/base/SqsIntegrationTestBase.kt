package uk.gov.justice.digital.hmpps.hmppshdcapi.integration.base

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.helpers.LocalStackContainer
import uk.gov.justice.digital.hmpps.hmppshdcapi.helpers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.EventProcessingComplete
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsSqsProperties
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SqsIntegrationTestBase : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @MockitoSpyBean
  protected lateinit var hmppsSqsPropertiesSpy: HmppsSqsProperties

  @MockitoBean
  lateinit var eventProcessingComplete: EventProcessingComplete

  protected val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents")
      ?: throw MissingQueueException("HmppsTopic domainevents not found")
  }
  protected val domainEventsTopicSnsClient by lazy { domainEventsTopic.snsClient }
  protected val domainEventsTopicArn by lazy { domainEventsTopic.arn }

  protected val mergeOffenderQueue by lazy { hmppsQueueService.findByQueueId("domaineventsqueue") as HmppsQueue }

  @BeforeEach
  fun cleanQueue() {
    mergeOffenderQueue.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(mergeOffenderQueue.queueUrl).build())
    await untilCallTo {
      mergeOffenderQueue.sqsClient.countMessagesOnQueue(mergeOffenderQueue.queueUrl).get()
    } matches { it == 0 }
  }

  fun getNumberOfMessagesCurrentlyOnQueue(): Int? = mergeOffenderQueue.sqsClient.countMessagesOnQueue(mergeOffenderQueue.queueUrl).get()

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @Suppress("unused")
    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
