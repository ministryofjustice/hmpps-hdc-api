package uk.gov.justice.digital.hmpps.hmppshdcapi.licences

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// Determining which type of set of conditions to loads requires the version field which is outside the json blob.
// This approach to prefer one type of conditions and fall back to the other is a bit naive but works.
class AdditionalConditionsDeserializer : JsonDeserializer<AdditionalConditions>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): AdditionalConditions {
    val node: JsonNode = p.codec.readTree(p)
    val mapper = (p.codec as ObjectMapper)

    return try {
      mapper.treeToValue(node, AdditionalConditionsV1::class.java)
    } catch (e: JsonProcessingException) {
      log.debug("Failed to deserialize AdditionalConditionsV1", e)
      try {
        mapper.treeToValue(node, AdditionalConditionsV2::class.java)
      } catch (e2: JsonProcessingException) {
        throw JsonMappingException(p, "Unable to deserialize AdditionalConditionsV2", e2)
      }
    }
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
