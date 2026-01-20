package controllers.schedules

import controllers.schedules.ClassesView.ClassView
import domain.Schedule.Class
import domain.ScheduleService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.response.*
import utils.DateUtils.parseIso

class ScheduleControllerImpl(
  private val scheduleService: ScheduleService,
) : ScheduleController {
  override suspend fun classes(call: ApplicationCall) {
    val date = call.request.queryParameters["date"]?.parseIso() ?: run {
      call.respond(BadRequest)
      return
    }

    val classes = scheduleService.scheduleForDate(date)

    if (classes.isNullOrEmpty()) call.respond(NotFound) else call.respond(OK, classes.toView())
  }

  private fun List<Class>.toView() = ClassesView(map { it.toView() })

  private fun Class.toView() = ClassView(
    subject = subject,
    teacher = teacher,
    date = date,
    startAt = period?.startAt,
    endAt = period?.endAt,
    type = ClassView.Type.valueOf(type.name),
    classroom = classroom,
  )
}
