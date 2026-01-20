package ktor

import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.MDC
import org.slf4j.event.Level
import java.util.*

fun CallLoggingConfig.loggerConfiguration() {
  level = Level.INFO
  mdc("startTime") { System.currentTimeMillis().toString() }
  mdc("requestId") { it.request.header("RequestId") ?: "self-${UUID.randomUUID()}" }
  format { call ->
    listOf(
      call.request.httpMethod.value,
      call.request.path(),
      call.request.queryString(),
      (call.response.status()?.toString() ?: "[unhandled]"),
      "${(System.currentTimeMillis() - MDC.get("startTime").toLong())} ms"
    ).filterNot { it.isEmpty() }.joinToString(" ")
  }
}
