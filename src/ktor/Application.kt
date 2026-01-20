package ktor

import com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*

fun Application.application() {
  install(DefaultHeaders)

  install(CallLogging) { loggerConfiguration() }

  install(ContentNegotiation) {
    jackson {
      val mapper = jacksonMapperBuilder()
        .enable(ACCEPT_CASE_INSENSITIVE_ENUMS)
        .disable(WRITE_DATES_AS_TIMESTAMPS)
        .addModule(JavaTimeModule())
        .build()

      register(ContentType.Application.Json, JacksonConverter(mapper))
    }
  }

  routing { routes() }
}

