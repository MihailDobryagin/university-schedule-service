package ktor.routes

import controllers.schedules.ScheduleController
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.schedules() {
  val controller by inject<ScheduleController>(ScheduleController::class.java)

  route("/classes") {
    get { controller.classes(call) }

    get("/by-period") { controller.classesByPeriod(call) }

    get("/raw") { controller.raw(call) }
  }
}
