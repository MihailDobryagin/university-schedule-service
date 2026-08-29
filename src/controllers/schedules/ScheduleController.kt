package controllers.schedules

import io.ktor.server.routing.*

interface ScheduleController {
  suspend fun raw(call: RoutingCall)
  suspend fun classes(call: RoutingCall)
  suspend fun classesByPeriod(call: RoutingCall)
}
