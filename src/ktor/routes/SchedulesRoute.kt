package ktor.routes

import controllers.schedules.ScheduleController
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.schedules() {
  val controller by inject<ScheduleController>(ScheduleController::class.java)

  get("/classes") { controller.classes(call) }
}
