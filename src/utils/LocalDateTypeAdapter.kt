package utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import utils.DateUtils.parseIso
import utils.DateUtils.toIso
import java.time.LocalDate

object LocalDateSerializer : JsonSerializer<LocalDate>() {
  override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(value.toIso())
  }
}

object LocalDateDeserializer : JsonDeserializer<LocalDate>() {
  override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDate {
    return parser.text.parseIso()
  }
}
