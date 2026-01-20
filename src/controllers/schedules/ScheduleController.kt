package controllers.schedules

import io.ktor.server.application.*

interface ScheduleController {
  suspend fun classes(call: ApplicationCall)
}
