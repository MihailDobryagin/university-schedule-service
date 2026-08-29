package controllers.schedules

import java.time.LocalDate

data class ScheduleView(val classesByDate: List<ClassesByDate>) {
  data class ClassesByDate(
    val date: LocalDate,
    val classes: List<ClassView>,
  )
}
