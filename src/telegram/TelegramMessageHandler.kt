package telegram

import org.telegram.telegrambots.meta.api.objects.Update

interface TelegramMessageHandler {
  suspend fun handle(update: Update)
}
