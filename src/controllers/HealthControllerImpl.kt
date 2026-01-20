package controllers

import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.response.*

class HealthControllerImpl : HealthController {
  override suspend fun health(call: ApplicationCall) {
    call.respond(OK, "OK")
  }
}
