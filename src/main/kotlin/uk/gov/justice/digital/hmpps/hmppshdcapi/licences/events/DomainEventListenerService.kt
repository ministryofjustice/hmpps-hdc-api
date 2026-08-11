package uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.MergePrisonerService
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HMPPSMergeDomainEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HMPPSMessage
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.events.dto.HMPPSPrisonerUpdateEvent
import uk.gov.justice.digital.hmpps.hmppshdcapi.licences.migration.MigrationProcessService

const val PRISONER_MERGE_EVENT_TYPE = "prison-offender-events.prisoner.merged"
const val PRISONER_RELEASED_EVENT = "prisoner-offender-search.prisoner.released"

fun interface EventProcessingComplete {
  fun complete()
}

private val NO_OP_FOR_TESTING = EventProcessingComplete { }

@Service
class DomainEventListenerService(
  private val mapper: ObjectMapper,
  private val mergePrisonerService: MergePrisonerService,
  private val migrateProcessService: MigrationProcessService,
  private val eventProcessingComplete: EventProcessingComplete = NO_OP_FOR_TESTING,
) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @SqsListener("domaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onEvent(requestJson: String) {
    try {
      val (message, messageAttributes) = mapper.readValue(requestJson, HMPPSMessage::class.java)
      val eventType = messageAttributes.eventType.Value
      log.info("Received message $message, type $eventType")
      when (eventType) {
        PRISONER_MERGE_EVENT_TYPE -> processPrisonerMergeEvent(message, eventType)
        PRISONER_RELEASED_EVENT -> processPrisonerReleaseEvent(message, eventType)
        else -> log.debug("Ignoring message with type $eventType")
      }
    } finally {
      // This is for testing and is within finally to ensure that the test knows it's complete even if there is an exception
      eventProcessingComplete.complete()
    }
  }

  private fun processPrisonerReleaseEvent(message: String, eventType: String) {
    val updateEvent = mapper.readValue(message, HMPPSPrisonerUpdateEvent::class.java)
    if (updateEvent.additionalInformation.reason.equals("RELEASED", ignoreCase = true)) {
      log.info("Processing release event message $message, type $eventType")
      migrateProcessService.migrateALicenceForPrisonerReleaseEvent(updateEvent.additionalInformation.nomsNumber)
    }
  }

  private fun processPrisonerMergeEvent(message: String, eventType: String) {
    log.info("Processing merge event message $message, type $eventType")
    val mergeEvent = mapper.readValue(message, HMPPSMergeDomainEvent::class.java)
    mergePrisonerService.mergePrisonerNumbers(
      mergeEvent.additionalInformation.removedNomsNumber,
      mergeEvent.additionalInformation.nomsNumber,
    )
  }
}
