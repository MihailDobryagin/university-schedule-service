package domain.crypto

import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import domain.admin.crypto.ReceiveSecretsCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import telegram.TelegramBot

class SecretsServiceImpl(
  private val bot: TelegramBot,
  private val chatId: Long,
  private val blockingCommandStorage: BlockingCommandStorage,
  private val secretsChannel: Channel<List<String>>,
  private val logger: Logger = LoggerFactory.getLogger(SecretsServiceImpl::class.java),
) : SecretsService {
  override suspend fun requestSecrets(): List<String> {
    while (!blockingCommandStorage.store(chatId, ReceiveSecretsCommand)) {
      logger.warn("Another command is waiting for TELEGRAM. Wait...")
      delay(500)
    }

    bot.sendMessage(chatId, "Request for secrets...")
    val secrets = secretsChannel.receive()
    secretsChannel.close()
    bot.sendMessage(chatId, "Secrets received.")
    return secrets
  }
}
