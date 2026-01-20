package domain.telegram

import com.mdobryagin.tgbots.callbacks.CallbackDataConverter
import domain.ScheduleService
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramBot
import java.time.LocalDate

class ScheduleCalendarRequestHandlerImpl(
  private val scheduleService: ScheduleService,
  private val bot: TelegramBot,
  private val callbackDataConverter: CallbackDataConverter,
  private val keyboardComposer: ScheduleKeyboardComposer,
  private val nowProvider: () -> LocalDate = LocalDate::now,
) : ScheduleCalendarRequestHandler {
  override suspend fun handle(update: Update) {
    val schedule = scheduleService.schedule()
    val today = nowProvider()
    val (year, month) = if (update.hasCallbackQuery())
      callbackDataConverter.parse(update.callbackQuery.data).args.let { it[0].toInt() to it[1].toInt() }
    else
      today.let { it.year to it.monthValue }

    val keyboard = keyboardComposer.compose(schedule, year, month)

    when {
      update.hasMessage() -> SendMessage.builder()
        .chatId(update.message.chatId)
        .text("Календарь")
        .replyMarkup(keyboard)
        .parseMode("HTML")
        .build()
        .also(bot::sendMessage)

      update.hasCallbackQuery() -> run {
        val callback = update.callbackQuery
        val message = callback.message
        EditMessageText.builder()
          .chatId(message.chatId)
          .messageId(message.messageId)
          .text("Календарь")
          .replyMarkup(keyboard)
          .parseMode("HTML")
          .build()
          .also(bot::editMessage)
      }
    }
  }
}
