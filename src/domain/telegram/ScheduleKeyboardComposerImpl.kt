package domain.telegram

import com.mdobryagin.tgbots.callbacks.CallbackData
import com.mdobryagin.tgbots.callbacks.CallbackDataConverter
import domain.Schedule
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import utils.DateUtils.toIso
import java.time.LocalDate
import java.time.Month
import java.time.Month.*

class ScheduleKeyboardComposerImpl(
  private val callbackDataConverter: CallbackDataConverter,
  private val nowProvider: () -> LocalDate = LocalDate::now,
) : ScheduleKeyboardComposer {
  override fun compose(schedule: Schedule?, year: Int, month: Int): InlineKeyboardMarkup {
    val today = nowProvider()
    val firstDayAtMonth = LocalDate.of(year, month, 1)
    val lastDatAtMonth = firstDayAtMonth.plusMonths(1).minusDays(1)
    val classes = (schedule?.classesByDay ?: emptyMap()).filterKeys { firstDayAtMonth <= it && it <= lastDatAtMonth }

    val monthHeader = button(firstDayAtMonth.month.i18n + " " + year)
    val daysOfWeekHeader = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").map(::button)
    val calendar = ArrayList<ArrayList<CalendarDay?>>(6)

    val previousMonth = firstDayAtMonth.minusMonths(1)
    val nextMonth = firstDayAtMonth.plusMonths(1)
    val navigationButtons = listOf(
      button("⬅", CallbackData("schedule_calendar", previousMonth.year, previousMonth.monthValue)),
      button("0⃣", CallbackData("schedule_calendar", today.year, today.monthValue)),
      button("➡", CallbackData("schedule_calendar", nextMonth.year, nextMonth.monthValue)),
    )

    run {
      var week = 0
      (firstDayAtMonth..lastDatAtMonth).forEach { date ->
        if (calendar.size == week) {
          ArrayList<CalendarDay?>(7).also { week -> (0 until 7).forEach { _ -> week.add(null) } }
            .also(calendar::add)
        }
        val dayOfWeek = date.dayOfWeek.value
        val classesForDay = classes[date]
        calendar[week][dayOfWeek - 1] = CalendarDay(date, classesForDay != null)
        if (dayOfWeek == 7) week++
      }
    }


    return InlineKeyboardMarkup.builder()
      .keyboardRow(listOf(monthHeader))
      .keyboardRow(daysOfWeekHeader)
      .keyboard(calendar.map { it.toButtons(today) })
      .keyboardRow(navigationButtons)
      .build()
  }

  private fun button(text: String) =
    InlineKeyboardButton.builder().text(text).callbackData("stub").build()

  private fun button(text: String, data: CallbackData) =
    InlineKeyboardButton.builder().text(text).callbackData(callbackDataConverter.compose(data)).build()

  private fun List<CalendarDay?>.toButtons(today: LocalDate) = map { day ->
    if (day == null) InlineKeyboardButton.builder().text("-").callbackData("stub").build()
    else {
      val date = day.date
      val text = if (date == today) "\uD83D\uDD06" else (if (day.hasClasses) "%s" else "|%s|").format(date.dayOfMonth)
      val callbackData = CallbackData("request_classes_from_menu", date.toIso())
      val callbackDataAsString = callbackDataConverter.compose(callbackData)
      InlineKeyboardButton.builder()
        .text(text)
        .callbackData(callbackDataAsString)
        .build()
    }
  }

  private fun ClosedRange<LocalDate>.forEach(block: (LocalDate) -> Unit) {
    var currentDate = start
    while (currentDate <= endInclusive) {
      block(currentDate)
      currentDate = currentDate.plusDays(1)
    }
  }

  private val Month.i18n: String
    get() = when (this) {
      JANUARY -> "Январь"
      FEBRUARY -> "Февраль"
      MARCH -> "Март"
      APRIL -> "Апрель"
      MAY -> "Май"
      JUNE -> "Июнь"
      JULY -> "Июль"
      AUGUST -> "Август"
      SEPTEMBER -> "Сентябрь"
      OCTOBER -> "Октябрь"
      NOVEMBER -> "Ноябрь"
      DECEMBER -> "Декабрь"
    }

  private data class CalendarDay(
    val date: LocalDate,
    val hasClasses: Boolean,
  )
}
