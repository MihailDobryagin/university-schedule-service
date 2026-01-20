package domain.admin.crypto

import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramMessageHandler
import telegram.TelegramMessageHandlerResolver

class CryptoTelegramMessagesResolver(
  private val blockingCommandStorage: BlockingCommandStorage,
  private val secretsResponseHandler: SecretsResponseHandler,
) : TelegramMessageHandlerResolver {
  override suspend fun resolveCritical(update: Update): TelegramMessageHandler? = null

  override suspend fun resolveNonBlocking(update: Update): TelegramMessageHandler? = null

  override suspend fun resolveBlocking(update: Update): TelegramMessageHandler? = when {
    update.message == null -> null
    blockingCommandStorage.getOrNull(update.message.chatId) is ReceiveSecretsCommand -> secretsResponseHandler
    else -> null
  }
}
