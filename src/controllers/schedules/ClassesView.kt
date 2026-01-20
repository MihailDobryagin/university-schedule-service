package controllers.schedules

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalTime

data class ClassesView(
  @JsonProperty("classes")
  val classes: List<ClassView>
) {
  data class ClassView(
    @JsonProperty("subject")
    val subject: String,
    @JsonProperty("teacher")
    val teacher: String,
    @JsonProperty("date")
    val date: LocalDate,
    @JsonProperty("startAt")
    val startAt: LocalTime?,
    @JsonProperty("endAt")
    val endAt: LocalTime?,
    @JsonProperty("type")
    val type: Type,
    @JsonProperty("classroom")
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
}
