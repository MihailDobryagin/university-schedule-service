package domain.telegram

import domain.Schedule.Class
import domain.Schedule.Class.Type.*
import utils.DateUtils.formatWithDots
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ClassesMessageComposerImpl : ClassesMessageComposer {
  private companion object {
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")
  }

  override fun compose(date: LocalDate, classes: List<Class>?): String {
    val header =
      "<b>%s (%s)</b>".format(date.formatWithDots(), date.dayOfWeek.i18n)
    val hr = "-".repeat(31)
    val classes = if (classes.isNullOrEmpty())
      "Нет занятий"
    else
      classes.joinToString(separator = "\n\n", transform = ::composeForClassInternal)

    return "$header\n$hr\n$classes"
  }

  override fun composeForClass(lesson: Class): String = composeForClassInternal(lesson)

  private fun composeForClassInternal(lesson: Class): String = with(lesson) {
    "<b>%s</b> %s (%s)\n%s\n<i>%s (%s)</i>".format(
      period?.let { period ->
        period.startAt.format(timeFormat) + (period.endAt?.let { "-" + it.format(timeFormat) } ?: "")
      } ?: "",
      teacher,
      group,
      subject,
      classroom,
      type.i18n,
    )
  }

  private val DayOfWeek.i18n: String
    get() = when (value) {
      1 -> "Пн"
      2 -> "Вт"
      3 -> "Ср"
      4 -> "Чт"
      5 -> "Пт"
      6 -> "Сб"
      7 -> "Вс"
      else -> throw IllegalStateException()
    }

  private val Class.Type.i18n: String
    get() = when (this) {
      LECTURE -> "Лекция"
      PRACTICE -> "Практика"
      CONSULTATION -> "Консультация"
      COURSE_WORK -> "Курсовая работа"
      CREDIT_WITH_ASSESSMENT -> "Зачёт с оценкой"
      CREDIT_WITHOUT_ASSESSMENT -> "Зачёт"
      EXAM -> "Экзамен"
      HEALTH_CHECK -> "Медицинский осмотр"
    }
}
