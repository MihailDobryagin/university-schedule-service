package domain.telegram

import domain.ScheduleService
import io.ktor.utils.io.core.*
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramBot

class RawScheduleRequestHandlerImpl(
  private val bot: TelegramBot,
  private val scheduleService: ScheduleService,
) : RawScheduleRequestHandler {
  override suspend fun handle(update: Update) {
    val schedule = scheduleService.htmlSchedule()
    val chatId = update.message.chatId

    if (schedule == null) bot.sendMessage(chatId, "No actual schedule")
    else bot.sendDocument(chatId, "actual-schedule.${schedule.checkSum}.html", schedule.html.toByteArray())
  }
}
