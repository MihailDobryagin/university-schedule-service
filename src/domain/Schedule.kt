package domain

import java.time.LocalDate
import java.time.LocalTime

data class Schedule(
  val classesByDay: Map<LocalDate, List<Class>>,
) {
  data class Class(
    val subject: String,
    val teacher: String,
    val date: LocalDate,
    val period: Period?,
    val group: String,
    val type: Type,
    val classroom: String,
  ) {
    enum class Type {
      LECTURE,
      PRACTICE,
      CONSULTATION,
      COURSE_WORK,
      CREDIT_WITH_ASSESSMENT,
      CREDIT_WITHOUT_ASSESSMENT,
      EXAM,
    }

    data class Period(
      val startAt: LocalTime,
      val endAt: LocalTime?,
    )
  }
}
