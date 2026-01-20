package domain.telegram

import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramBot

class HealthMessageHandlerImpl(
  private val bot: TelegramBot,
) : HealthMessageHandler {
  override suspend fun handle(update: Update) = bot.sendMessage(update.message.chatId, "OK")
}
