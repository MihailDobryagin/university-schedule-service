package domain.admin.telegram

import domain.MonitoringService
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramBot
import utils.TelegramUtils.textWithoutCommand

class WhiteListMessageHandlerImpl(
  private val bot: TelegramBot,
  private val repository: WhiteListRepository,
  private val monitoringService: MonitoringService,
) : WhiteListMessageHandler {
  private companion object {
    const val COMMAND = "add_to_whitelist"
  }

  override suspend fun handle(update: Update) {
    val message = update.message
    val chatId = message.chatId
    val username = update.textWithoutCommand(COMMAND)
    if (username.isBlank()) {
      monitoringService.submitError("No username on add-to-whitelist command")
      return
    }
    repository.addToWhiteList(username)
    bot.sendMessage(chatId, "<u><b>$username</b></u> added to white-list")
  }
}
