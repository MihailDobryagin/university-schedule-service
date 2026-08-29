package controllers.schedules

import domain.Schedule.Class
import domain.ScheduleService
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import utils.DateUtils.parseIso
import java.time.LocalDate

class ScheduleControllerImpl(
  private val service: ScheduleService,
) : ScheduleController {
  override suspend fun raw(call: RoutingCall) {
    val schedule = service.htmlSchedule()
    if (schedule == null) {
      call.respond(NotFound)
    } else {
      val view = with(schedule) { HtmlScheduleView(html = html, checkSum = checkSum) }
      call.respond(OK, view)
    }
  }

  override suspend fun classes(call: RoutingCall) {
    val date = call.queryParameters.getOrFail("date").parseIso()

    val classes = service.scheduleForDate(date)

    if (classes.isNullOrEmpty()) call.respond(NotFound) else call.respond(OK, classes.toView())
  }

  override suspend fun classesByPeriod(call: RoutingCall) {
    val (from, to) = with(call.queryParameters) { getOrFail("from").parseIso() to getOrFail("to").parseIso() }

    val schedule = service.scheduleByPeriod(from, to)

    call.respond(OK, schedule.toScheduleView())
  }

  private fun List<Class>.toView() = ClassesView(map { it.toView() })

  private fun Map<LocalDate, List<Class>>.toScheduleView() =
    mapValues { (_, classes) -> classes.map { it.toView() } }
      .map { (date, classesView) -> ScheduleView.ClassesByDate(date, classesView) }
      .let { ScheduleView(it) }

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
