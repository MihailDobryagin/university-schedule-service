package utils

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

object TelegramUtils {
  fun Message?.checkForOperation(operation: String, prefix: Boolean = false) = this != null
    && (
    isCommand && (if (prefix) text.startsWith("/$operation") else text == "/$operation")
      || !isCommand && if (prefix) text.startsWith(operation) else text == operation
    )

  fun Update.textWithoutCommand(command: String) = message.text.replace("^/?$command ?".toRegex(), "")
}
