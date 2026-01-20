package domain.admin.telegram

import ManageCommand
import ManageCommand.STOP
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

class StopMessageHandlerImpl(
  private val manageCommands: Queue<ManageCommand>,
) : StopMessageHandler {
  override suspend fun handle(update: Update) {
    manageCommands.offer(STOP)
  }
}
