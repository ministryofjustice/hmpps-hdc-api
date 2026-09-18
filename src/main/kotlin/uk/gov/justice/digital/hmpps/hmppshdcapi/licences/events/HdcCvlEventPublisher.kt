package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HdcCvlQueueEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.request.HdcCvlEventRequest
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.time.LocalDateTime

private const val HDC_CVL_EVENTS_QUEUE_ID = "hdccvleventsqueue"
private const val HDC_CVL_EVENT_VERSION = 1

@Service
class HdcCvlEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  fun publish(request: HdcCvlEventRequest): HdcCvlQueueEvent {
    val event = HdcCvlQueueEvent(
      version = HDC_CVL_EVENT_VERSION,
      occurredAt = LocalDateTime.now(),
      licenceId = requireNotNull(request.licenceId),
      bookingId = requireNotNull(request.bookingId),
      nomsNumber = requireNotNull(request.nomsNumber),
      triggeredBy = requireNotNull(request.triggeredBy),
      reason = request.reason,
    )

    val queue = hmppsQueueService.findByQueueId(HDC_CVL_EVENTS_QUEUE_ID)
      ?: throw MissingQueueException("HmppsQueue $HDC_CVL_EVENTS_QUEUE_ID not found")

    val eventType = requireNotNull(request.eventType).name
    queue.sqsClient.sendMessage(
      SendMessageRequest.builder()
        .queueUrl(queue.queueUrl)
        .messageBody(objectMapper.writeValueAsString(event))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        )
        .build(),
    )

    log.info("Published HDC to CVL event {} for licence {}", eventType, event.licenceId)
    return event
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
