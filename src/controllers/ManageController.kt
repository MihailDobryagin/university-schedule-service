package controllers

import io.ktor.server.application.ApplicationCall

interface ManageController {
  fun stop(call: ApplicationCall)
}
