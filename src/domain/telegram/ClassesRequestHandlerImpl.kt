package domain.telegram

import domain.ScheduleService
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramBot
import utils.TelegramUtils.textWithoutCommand
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

class ClassesRequestHandlerImpl(
  private val scheduleService: ScheduleService,
  private val classesMessageComposer: ClassesMessageComposer,
  private val bot: TelegramBot,
  private val nowProvider: () -> LocalDate = LocalDate::now,
) : ClassesRequestHandler {
  private companion object {
    const val COMMAND = "classes"
    val ISO_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val DOTS_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
  }

  private val dotsFormatWithDefaultYear: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("dd.MM")
    .parseDefaulting(ChronoField.YEAR, nowProvider().year.toLong())
    .toFormatter()

  override suspend fun handle(update: Update) {
    val dateParam = update.textWithoutCommand(COMMAND)
    val date = when {
      dateParam.isBlank() -> nowProvider()
      dateParam == "tomorrow" -> nowProvider().plusDays(1)
      else -> dateParam.parseDateParam()
    }
    val chatId = update.message.chatId
    val classes = scheduleService.scheduleForDate(date)

    bot.sendMessage(chatId, classesMessageComposer.compose(date, classes))
  }

  private fun String.parseDateParam() = try {
    LocalDate.parse(this, ISO_FORMAT)
  } catch (_: DateTimeParseException) {
    try {
      LocalDate.parse(this, DOTS_FORMAT)
    } catch (_: DateTimeParseException) {
      LocalDate.parse(this, dotsFormatWithDefaultYear)
    }
  }
}
