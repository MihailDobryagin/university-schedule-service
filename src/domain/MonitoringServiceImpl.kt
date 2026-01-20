package domain

import com.mdobryagin.mails.Message
import com.mdobryagin.mails.MessageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MonitoringServiceImpl(
  private val monitoringEmails: List<String>,
  private val messageService: MessageService,
  private val logger: Logger = LoggerFactory.getLogger(MonitoringServiceImpl::class.java),
) : MonitoringService {
  override fun submitError(error: String) {
    logger.error(error)
    val email = Message(monitoringEmails, "Schedule notify ERROR", error)
    messageService.sendMessage(email)
  }

  override fun submitError(error: String, exception: Throwable) {
    logger.error(error, exception)
    val email = Message(monitoringEmails, "Schedule notify ERROR", error + "\n" + exception.stackTraceToString())
    messageService.sendMessage(email)
  }

  override fun submitHeartBeat() {
    logger.info("Heartbeat")
    val email = Message(listOf("dobryagin.mihail12@mail.ru"), "HEARTBEAT", "")
    messageService.sendMessage(email)
  }
}
