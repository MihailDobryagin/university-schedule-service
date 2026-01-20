package domain.repository

import domain.Schedule
import domain.Schedule.Class
import domain.Schedule.Class.Period
import domain.Schedule.Class.Type
import domain.Schedule.Class.Type.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleHtmlConverterImpl(
  private val logger: Logger = LoggerFactory.getLogger(ScheduleHtmlConverterImpl::class.java),
) : ScheduleHtmlConverter {
  private companion object {
    val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")
  }

  override fun fromHtml(html: String): Schedule {
    val document = Jsoup.parse(html)
    val classes = document.select("tbody tr")
      .mapNotNull { row ->
        val day = row.selectFirst(".column-2")!!.text().toInt()
        val month = row.selectFirst(".column-3")!!.text().toInt()
        val formattedPeriod = row.selectFirst(".column-4")!!.text()
        val group = row.selectFirst(".column-5")!!.text()
        val type = row.selectFirst(".column-6")!!.text().toType()
        val subject = row.selectFirst(".column-7")!!.text()
        val teacher = row.selectFirst(".column-9")!!.text()
        val classroom = row.selectFirst(".column-10")!!.text()
        val date = LocalDate.of(if (month < 8) 2026 else 2025, month, day)
        val period = formattedPeriod.toPeriod()

        if (type == null) {
          logger.warn("No type for subject '$subject' at '$day.$month'")
          null
        } else Class(
          subject = subject,
          teacher = teacher,
          date = date,
          period = period,
          group = group,
          type = type,
          classroom = classroom,
        )
      }
      .filter { "01" in it.group }

    return Schedule(classes.groupBy(Class::date))
  }

  override fun toHtml(schedule: Schedule): String = Jsoup.parse("").apply {
    val table = appendElement("table").apply {
      appendElement("thead").apply {
        appendElement("tr").apply {
          listOf(
            "ДеньНедели",
            "Дата",
            "Месяц",
            "Время",
            "Группы",
            "ТипЗанятий",
            "Предмет",
            "ДолжностьПреподавателя",
            "Преподаватель",
            "Аудитория",
          )
            .map { title -> createElement("th").apply { text(title) } }
            .forEach(::appendChild)
        }
      }
      appendElement("tbody").apply {
        schedule.classesByDay
          .asSequence()
          .sortedBy { it.key }
          .flatMap { dateWithClasses -> dateWithClasses.value.sortedBy { it.period?.startAt } }
          .map { classToRow(it) }
          .forEach(::appendChild)

      }
    }
    selectFirst("body")!!.appendChild(table)
  }.html()

  private fun Document.classToRow(lesson: Class) = createElement("tr").apply {
    with(lesson) {
      listOf<String>(
        date.dayOfWeek.i18n,
        String.format("%02d", date.dayOfMonth),
        String.format("%02d", date.month.value),
        period?.let { period ->
          period.startAt.format(timeFormat) + (period.endAt?.let { "-" + it.format(timeFormat) } ?: "")
        } ?: "",
        group,
        type.i18n,
        subject,
        "-",
        teacher,
        classroom,
      )
        .forEachIndexed { index, text ->
          appendElement("td").apply {
            attr("class", "column-${index + 1}")
            text(text)
          }
        }
    }
  }

  private fun String.toType(): Type? = when (uppercase()) {
    "Л" -> LECTURE
    "ПЗ" -> PRACTICE
    "К" -> CONSULTATION
    "КР" -> COURSE_WORK
    "ЗО" -> CREDIT_WITH_ASSESSMENT
    "З" -> CREDIT_WITHOUT_ASSESSMENT
    "Э" -> EXAM
    "" -> null
    else -> throw IllegalStateException("No mapping for class-type")
  }


  private fun String.toPeriod(): Period? {
    if (length < 5) return null

    val startAt = take(5).let { LocalTime.parse(it, timeFormat) }
    val endAt = if (length < 10)
      null
    else
      takeLast(5).let { LocalTime.parse(it, timeFormat) }

    return Period(startAt, endAt)
  }

  private val DayOfWeek.i18n
    get() = when (this) {
      MONDAY -> "Пн"
      TUESDAY -> "Вт"
      WEDNESDAY -> "Ср"
      THURSDAY -> "Чт"
      FRIDAY -> "Пт"
      SATURDAY -> "Сб"
      SUNDAY -> "Вс"
    }

  private val Type.i18n
    get() = when (this) {
      LECTURE -> "Л"
      PRACTICE -> "ПЗ"
      CONSULTATION -> "К"
      COURSE_WORK -> "КР"
      CREDIT_WITH_ASSESSMENT -> "ЗО"
      CREDIT_WITHOUT_ASSESSMENT -> "З"
      EXAM -> "Э"
    }
}
