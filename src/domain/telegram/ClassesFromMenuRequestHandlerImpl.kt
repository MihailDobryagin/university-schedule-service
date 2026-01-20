package domain.telegram

import com.mdobryagin.tgbots.callbacks.CallbackData
import com.mdobryagin.tgbots.callbacks.CallbackDataConverter
import domain.ScheduleService
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import telegram.TelegramBot
import utils.DateUtils.parseIso
import utils.DateUtils.toIso
import java.time.LocalDate

class ClassesFromMenuRequestHandlerImpl(
  private val callbackDataConverter: CallbackDataConverter,
  private val bot: TelegramBot,
  private val scheduleService: ScheduleService,
  private val classesMessageComposer: ClassesMessageComposer,
  private val nowProvider: () -> LocalDate = LocalDate::now,
) : ClassesFromMenuRequestHandler {
  override suspend fun handle(update: Update) {
    val callback = update.callbackQuery
    val args = callbackDataConverter.parse(callback.data).args
    val date = args[0].parseIso()
    val classes = scheduleService.scheduleForDate(date)
    val chatId = callback.message.chatId

    val newMessage = EditMessageText.builder()
      .chatId(chatId)
      .messageId(callback.message.messageId)
      .text(classesMessageComposer.compose(date, classes))
      .parseMode("HTML")
      .replyMarkup(calendarKeyboard(date))
      .build()

    bot.editMessage(newMessage)
  }

  private fun calendarKeyboard(date: LocalDate): InlineKeyboardMarkup {
    val calendarBtn =
      CallbackData("schedule_calendar", date.year.toString(), date.monthValue.toString()).toBtn("Календарь")
    val yesterdayBtn =
      CallbackData("request_classes_from_menu", date.minusDays(1).toIso()).toBtn("⬅")
    val todayBtn =
      CallbackData("request_classes_from_menu", nowProvider().toIso()).toBtn("0⃣")
    val tomorrowBtn =
      CallbackData("request_classes_from_menu", date.plusDays(1).toIso()).toBtn("➡")

    return InlineKeyboardMarkup.builder()
      .keyboardRow(listOf(yesterdayBtn, todayBtn, tomorrowBtn))
      .keyboardRow(listOf(calendarBtn))
      .build()
  }

  private fun CallbackData.toBtn(text: String) = InlineKeyboardButton.builder()
    .text(text)
    .callbackData(callbackDataConverter.compose(this))
    .build()
}
