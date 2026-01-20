package utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

object LocalTimeSerializer : JsonSerializer<LocalTime>() {
  override fun serialize(value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(value.format(timeFormatter))
  }
}

object LocalTimeDeserializer : JsonDeserializer<LocalTime>() {
  override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalTime {
    return LocalTime.parse(parser.text, timeFormatter)
  }
}
