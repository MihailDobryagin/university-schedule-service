package ktor

import io.ktor.server.routing.*
import ktor.routes.health
import ktor.routes.manage
import ktor.routes.schedules

fun Routing.routes() {
  route("/health") { health() }
  route("/manage") { manage() }
  route("/schedule") { schedules() }
}
