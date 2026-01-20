package controllers

import io.ktor.server.application.ApplicationCall

interface HealthController {
  suspend fun health(call: ApplicationCall)
}
