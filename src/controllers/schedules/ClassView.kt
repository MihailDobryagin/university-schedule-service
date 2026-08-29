package controllers.schedules

import java.time.LocalDate
import java.time.LocalTime

data class ClassView(
  val subject: String,
  val teacher: String,
  val date: LocalDate,
  val startAt: LocalTime?,
  val endAt: LocalTime?,
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
}
