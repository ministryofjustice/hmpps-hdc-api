package uk.gov.justice.digital.hmpps.hmppshdcapi.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.slf4j.LoggerFactory
import java.time.LocalTime

class HdcJsonLocalTimeDeserializer : JsonDeserializer<LocalTime?>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalTime? = try {
    val text = p.text
    if (text.isNullOrBlank()) null else LocalTime.parse(text)
  } catch (e: Exception) {
    val path = p.parsingContext?.pathAsPointer()?.toString()
      ?: p.currentLocation()?.toString()
    log.error("Error parsing time at: {} value of {}", path, p.text)
    // Should this be null or perhaps start of day 0?
    null
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
