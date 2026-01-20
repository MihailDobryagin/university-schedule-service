package domain.config

import config.ConfigService
import telegram.TelegramBot

class ShutdownConfigProcessor(
  private val adminChatId: Long,
  private val service: ConfigService,
  private val bot: TelegramBot,
) {
  suspend fun process() {
    val configsJson = service.configsJson()
    bot.sendMessage(adminChatId, configsJson.toString())
  }
}
