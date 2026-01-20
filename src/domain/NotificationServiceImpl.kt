package domain

import domain.telegram.ClassesMessageComposer
import telegram.TelegramBot
import java.time.LocalDate

class NotificationServiceImpl(
  private val classesMessageComposer: ClassesMessageComposer,
  private val chatiIds: Set<Long>,
  private val bot: TelegramBot,
) : NotificationService {
  override suspend fun notifyUpcomingClasses(classesDate: LocalDate, classes: List<Schedule.Class>) {
    val header = "🔜"
    val body = classesMessageComposer.compose(classesDate, classes)
    val message = "$header\n\n$body"
    chatiIds.forEach { bot.sendMessage(it, message) }
  }

  override suspend fun notifyClass(lesson: Schedule.Class) {
    val header = "🔔"
    val body = classesMessageComposer.composeForClass(lesson)
    val message = "$header\n$body"
    chatiIds.forEach { bot.sendMessage(it, message) }
  }
}
