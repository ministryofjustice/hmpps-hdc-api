package uk.gov.justice.digital.hmpps.hmppshdcapi.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.io.IOException

@JsonDeserialize(using = StringListHolderDeserializer::class)
@JsonSerialize(using = StringListHolderSerializer::class)
data class StringListHolder(val items: List<String>, val singleItem: Boolean) {
  constructor(item: String) : this(listOf(item), singleItem = true)
  constructor(items: Iterator<String>) : this(items.asSequence().toList(), singleItem = false)
}

class StringListHolderDeserializer : JsonDeserializer<StringListHolder?>() {
  @Throws(IOException::class)
  override fun deserialize(parser: JsonParser, context: DeserializationContext?) = if (parser.isExpectedStartArrayToken) {
    parser.nextToken()
    StringListHolder(parser.readValuesAs(String::class.java))
  } else {
    StringListHolder(parser.readValueAs(String::class.java))
  }
}

class StringListHolderSerializer : JsonSerializer<StringListHolder>() {
  @Throws(IOException::class)
  override fun serialize(value: StringListHolder, jgen: JsonGenerator, provider: SerializerProvider?) {
    when {
      value.singleItem -> jgen.writeString(value.items[0])
      else -> jgen.writeArray(value.items.toTypedArray(), 0, value.items.size)
    }
  }
}
