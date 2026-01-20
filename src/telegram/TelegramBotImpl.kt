package telegram

import com.mdobryagin.tgbots.DefaultActiveTelegramBot
import com.mdobryagin.tgbots.TelegramBotInfo
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDateTime
import java.time.ZoneOffset

class TelegramBotImpl(
  botInfo: TelegramBotInfo,
  private val adminUsername: String,
  private val allowedChats: Set<Long>,
  private val nowProvider: () -> LocalDateTime = LocalDateTime::now,
  private val zoneOffset: ZoneOffset = ZoneOffset.systemDefault().rules.getOffset(nowProvider()),
  private val logger: Logger = LoggerFactory.getLogger(TelegramBot::class.java),
) : TelegramBot, DefaultActiveTelegramBot(botInfo) {
  private val startedAt = nowProvider()
  private val resolvers = mutableListOf<TelegramMessageHandlerResolver>()
  private val allowedUsersProviders = mutableListOf<suspend () -> Set<String>>()

  override fun registerResolver(resolver: TelegramMessageHandlerResolver) {
    resolvers.add(resolver)
  }

  override fun registerAllowedUsersProvider(provider: suspend () -> Set<String>) {
    allowedUsersProviders.add(provider)
  }

  override fun process(update: Update) {
    when {
      update.hasMyChatMember() -> {
        val chat = update.myChatMember.chat
        UpdateInfo(chat.id, chat.userName, dateTime(update.message.date))
      }

      update.hasMessage() -> {
        val chat = update.message.chat
        UpdateInfo(chat.id, chat.userName, dateTime(update.message.date))
      }

      update.hasCallbackQuery() -> {
        val callback = update.callbackQuery
        UpdateInfo(callback.message.chatId, callback.from.userName, dateTime(callback.message.date))
      }

      else -> null
    }?.also { info ->
      val username = info.username
      val chatId = info.chatId
      if (
        !(
          username == adminUsername
            || username in runBlocking { allowedUsersProviders.flatMap { it() } }
            || chatId in allowedChats
          )
      ) {
        logger.error("Unknown user (name='$username', chatId='$chatId'")
        return
      }

      if (info.time < startedAt) {
        logger.warn("Skip old message (updateId=${update.updateId})")
        return
      }
    }


    runBlocking {
      val handler = resolvers.firstNotNullOfOrNull { it.resolveCritical(update) }
        ?: resolvers.firstNotNullOfOrNull { it.resolveBlocking(update) }
        ?: resolvers.firstNotNullOfOrNull { it.resolveNonBlocking(update) }
        ?: run {
          logger.warn("Can't find handler for TELEGRAM message $update")
          return@runBlocking
        }
      handler.handle(update)
    }
  }

  private fun dateTime(seconds: Int) = LocalDateTime.ofEpochSecond(seconds.toLong(), 0, zoneOffset)

  private data class UpdateInfo(
    val chatId: Long,
    val username: String?,
    val time: LocalDateTime,
  )
}
