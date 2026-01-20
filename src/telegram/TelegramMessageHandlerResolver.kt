package telegram

import org.telegram.telegrambots.meta.api.objects.Update

interface TelegramMessageHandlerResolver {
  suspend fun resolveCritical(update: Update): TelegramMessageHandler?
  suspend fun resolveNonBlocking(update: Update): TelegramMessageHandler?
  suspend fun resolveBlocking(update: Update): TelegramMessageHandler?
}
