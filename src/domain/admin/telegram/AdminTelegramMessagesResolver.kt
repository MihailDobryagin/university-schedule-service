package domain.admin.telegram

import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import domain.config.admin.ReceiveConfigCommand
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramMessageHandler
import telegram.TelegramMessageHandlerResolver
import utils.TelegramUtils.checkForOperation

class AdminTelegramMessagesResolver(
  private val adminUsername: String,
  private val stopMessageHandler: StopMessageHandler,
  private val whiteListMessageHandler: WhiteListMessageHandler,
  private val loadConfigResponseHandler: LoadConfigResponseHandler,
  private val blockingCommandStorage: BlockingCommandStorage,
) : TelegramMessageHandlerResolver {
  override suspend fun resolveCritical(update: Update): TelegramMessageHandler? = resolveWithRightsCheck(update) {
    when {
      message.checkForOperation("stop") -> stopMessageHandler
      else -> null
    }
  }

  override suspend fun resolveNonBlocking(update: Update): TelegramMessageHandler? = resolveWithRightsCheck(update) {
    when {
      message.checkForOperation("add_to_whitelist", prefix = true) -> whiteListMessageHandler
      else -> null
    }
  }

  override suspend fun resolveBlocking(update: Update): TelegramMessageHandler? = resolveWithRightsCheck(update) {
    when {
      update.message == null -> null
      blockingCommandStorage.getOrNull(update.message.chatId) is ReceiveConfigCommand -> loadConfigResponseHandler
      else -> null
    }
  }

  private fun resolveWithRightsCheck(
    update: Update, block: Update.() -> TelegramMessageHandler?
  ): TelegramMessageHandler? = if (preCheck(update)) update.block() else null

  private fun preCheck(update: Update) = update.message?.chat?.userName == adminUsername
}
