package modules

import com.mdobryagin.tgbots.TelegramBotInfo
import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import com.mdobryagin.tgbots.blockingcommands.DefaultBlockingCommandStorage
import org.koin.dsl.module
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import telegram.TelegramBot
import telegram.TelegramBotImpl
import utils.SystemUtils.env

val TelegramModule = module(createdAtStart = true) {
  val bot = TelegramBotImpl(
    TelegramBotInfo(token = env("TELEGRAM_TOKEN"), name = env("TELEGRAM_USERNAME")),
    env("TELEGRAM_ADMIN"),
    env("TELEGRAM_ALLOWED_CHATS").split(";").map { it.toLong() }.toSet(),
  )
  TelegramBotsApi(DefaultBotSession::class.java).registerBot(bot)

  single<TelegramBot>(createdAtStart = true) { bot }
  single<BlockingCommandStorage> { DefaultBlockingCommandStorage() }
}
