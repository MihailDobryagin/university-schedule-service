package domain.admin.crypto

import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import kotlinx.coroutines.channels.Channel
import org.telegram.telegrambots.meta.api.objects.Update

class SecretsResponseHandlerImpl(
  private val configsChannel: Channel<List<String>>,
  private val blockingCommandStorage: BlockingCommandStorage,
) : SecretsResponseHandler {
  override suspend fun handle(update: Update) {
    val message = update.message
    configsChannel.send(message.text.split('\n'))
    blockingCommandStorage.clear(message.chatId)
  }
}
