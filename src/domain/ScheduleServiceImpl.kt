package domain

import com.mdobryagin.mails.Message
import com.mdobryagin.mails.MessageService
import domain.Schedule.Class
import domain.repository.HtmlSchedule
import domain.repository.RawSchedule
import domain.repository.ScheduleRepository
import domain.repository.ScheduleRepository.UpdateScheduleResult.HasNewScheduleResult
import domain.repository.ScheduleRepository.UpdateScheduleResult.NoScheduleResult
import telegram.TelegramBot
import java.time.LocalDate

class ScheduleServiceImpl(
  private val subscribersEmails: List<String>,
  private val subscribersTelegramChatIds: List<Long>,
  private val messageService: MessageService,
  private val monitoringService: MonitoringService,
  private val bot: TelegramBot,
  private val repository: ScheduleRepository,
) : ScheduleService {
  override suspend fun checkSchedule() {
    val newSchedule = repository.checkAndUpdateSchedule()
      .also { result ->
        if (result is HasNewScheduleResult) return@also

        if (result is NoScheduleResult) notifyNoSchedule()
        return
      } as HasNewScheduleResult

    val checkSum = newSchedule.html.checkSum

    val message = Message(
      subscribersEmails,
      "Расписание поменялось (v.${newSchedule.number}, ch.$checkSum)",
      newSchedule.html.html,
      html = true,
    )
    messageService.sendMessage(message)

    subscribersTelegramChatIds.forEach { chat -> bot.sendMessage(chat, "Расписание поменялось") }
  }

  override suspend fun rawSchedule(): RawSchedule? = repository.actualRawSchedule()

  override suspend fun htmlSchedule(): HtmlSchedule? = repository.actualHtmlSchedule()

  override suspend fun schedule(): Schedule? = repository.actualSchedule()

  override suspend fun scheduleForDate(date: LocalDate): List<Class>? =
    repository.actualSchedule()?.classesByDay?.get(date)

  override suspend fun scheduleByPeriod(from: LocalDate, to: LocalDate): Map<LocalDate, List<Class>> =
    repository.actualSchedule()?.classesByDay.orEmpty().filterKeys { date -> date in (from..to) }

  private fun notifyNoSchedule() {
    monitoringService.submitError("Расписание пропало")
  }

  private val String.isActualSchedule: Boolean
    get() = "<td class=\"column-3\">09</td>" in this || "09.2025" in this
}
