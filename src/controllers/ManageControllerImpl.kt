package controllers

import domain.AppManagingService
import io.ktor.server.application.*

class ManageControllerImpl(private val service: AppManagingService) : ManageController {
  override fun stop(call: ApplicationCall) {
    service.stop()
  }
}
