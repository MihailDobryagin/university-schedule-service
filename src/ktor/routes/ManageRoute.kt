package ktor.routes

import controllers.ManageController
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.manage() {
  val controller by inject<ManageController>(ManageController::class.java)

  post("/stop") { controller.stop(call) }
}
