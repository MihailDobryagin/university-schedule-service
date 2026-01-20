package domain.telegram

import com.mdobryagin.tgbots.callbacks.CallbackDataConverter
import org.telegram.telegrambots.meta.api.objects.Update
import telegram.TelegramMessageHandler
import telegram.TelegramMessageHandlerResolver
import utils.TelegramUtils.checkForOperation

class DomainTelegramMessagesResolver(
  private val healthMessageHandler: HealthMessageHandler,
  private val rawScheduleRequestHandler: RawScheduleRequestHandler,
  private val classesRequestHandler: ClassesRequestHandler,
  private val scheduleCalendarRequestHandler: ScheduleCalendarRequestHandler,
  private val classesFromMenuRequestHandler: ClassesFromMenuRequestHandler,
  private val callbackDataConverter: CallbackDataConverter,
) : TelegramMessageHandlerResolver {
  override suspend fun resolveCritical(update: Update): TelegramMessageHandler? {
    val message = update.message
    return when {
      message.checkForOperation("health") -> healthMessageHandler
      else -> null
    }
  }

  override suspend fun resolveNonBlocking(update: Update): TelegramMessageHandler? {
    val message = update.message
    return when {
      message.checkForOperation("actual_schedule") -> rawScheduleRequestHandler
      message.checkForOperation("classes", prefix = true) -> classesRequestHandler
      message.checkForOperation("schedule_calendar") ->
        scheduleCalendarRequestHandler

      update.callbackQuery != null -> {
        val callback = callbackDataConverter.parse(update.callbackQuery.data)
        when {
          callback.command == "request_classes_from_menu" -> classesFromMenuRequestHandler
          callback.command == "schedule_calendar" -> scheduleCalendarRequestHandler
          else -> null
        }
      }

      else -> null
    }
  }

  override suspend fun resolveBlocking(update: Update): TelegramMessageHandler? = null
}
