package domain

import ManageCommand
import ManageCommand.STOP
import java.util.*

class AppManagingServiceImpl(private val manageCommands: Queue<ManageCommand>) : AppManagingService {
  override fun stop() {
    manageCommands.add(STOP)
  }
}
