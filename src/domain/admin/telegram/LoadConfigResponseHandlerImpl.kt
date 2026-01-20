package domain.admin.telegram

import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import kotlinx.coroutines.channels.Channel
import org.telegram.telegrambots.meta.api.objects.Update

class LoadConfigResponseHandlerImpl(
  private val configsChannel: Channel<String>,
  private val blockingCommandStorage: BlockingCommandStorage,
) : LoadConfigResponseHandler {
  override suspend fun handle(update: Update) {
    val message = update.message
    configsChannel.send(message.text)
    blockingCommandStorage.clear(message.chatId)
  }
}
