package ktor.routes

import controllers.HealthController
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject

fun Route.health() {
  val controller by inject<HealthController>(HealthController::class.java)

  get { controller.health(call) }
}
