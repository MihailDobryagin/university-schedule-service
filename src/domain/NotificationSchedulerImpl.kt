package domain

import domain.Schedule.Class
import domain.Schedule.Class.Type
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class NotificationSchedulerImpl(
  private val scheduleService: ScheduleService,
  private val notificationService: NotificationService,
  private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1),
  private val nowProvider: () -> LocalDateTime = LocalDateTime::now,
  private val logger: Logger = LoggerFactory.getLogger(NotificationSchedulerImpl::class.java),
) : NotificationScheduler {
  private companion object {
    const val NOTIFICATION_EPS_SEC = 3
  }

  private val todayNotifs = mutableMapOf<ClassIdentifier, ScheduledNotification>()
  private var classesNotif: ScheduledNotification? = null

  override suspend fun reSchedule() {
    revokeOldNotifications()

    val now = nowProvider()
    val today = now.toLocalDate()
    val tomorrow = today.plusDays(1)
    val scheduleForTomorrow = scheduleService.scheduleForDate(tomorrow)
    val scheduleForToday = scheduleService.scheduleForDate(today)

    scheduleForTomorrow.also { reScheduleClassesNotification(tomorrow, scheduleForTomorrow.orEmpty()) }
    scheduleForToday?.forEach(::reScheduleClassesRemembering)
  }

  private fun reScheduleClassesNotification(classesDate: LocalDate, classes: List<Class>) {
    val now = nowProvider()
    classesNotif?.also { currentNotif ->
      if (currentNotif.canBeInProcess(now)) return
      currentNotif.future.cancel(false)
      classesNotif = null
    }

    val notifyAt = now.withHour(20).withMinute(0).withSecond(0)
    val duration = Duration.between(now, notifyAt)
    if (duration.toMillis() < -1500) return

    classesNotif = ScheduledNotification(
      notifyAt = notifyAt,
      future = scheduledExecutor.schedule(
        { runBlocking { notificationService.notifyUpcomingClasses(classesDate, classes) } },
        duration.toMillis(), TimeUnit.MILLISECONDS
      )
    )
  }

  private fun reScheduleClassesRemembering(lesson: Class) {
    val lessonIdentifier = lesson.identifier
    val now = nowProvider()
    todayNotifs[lessonIdentifier]?.also { currentNotification ->
      if (currentNotification.canBeInProcess(now)) return
      currentNotification.future.cancel(false)
      todayNotifs.remove(lessonIdentifier)
    }
    val startedAt = lesson.period?.startAt?.let {
      now.withHour(it.hour).withMinute(it.minute).withSecond(0)
    } ?: run {
      logger.warn("Can't create notification for $lessonIdentifier because of not valid period")
      return
    }

    val notifyAt = startedAt.minusMinutes(10)
    val duration = Duration.between(now, notifyAt)
    if (duration.toMillis() < -1500) return

    todayNotifs[lesson.identifier] = ScheduledNotification(
      notifyAt = notifyAt,
      future = scheduledExecutor.schedule(
        { runBlocking { notificationService.notifyClass(lesson) } },
        duration.toMillis(), TimeUnit.MILLISECONDS
      )
    )
  }

  private fun revokeOldNotifications() {
    val today = nowProvider().toLocalDate()

    if (classesNotif?.let { it.notifyAt.toLocalDate() < today } ?: false) classesNotif = null

    todayNotifs
      .filterValues { it.notifyAt.toLocalDate() < today }
      .keys
      .forEach(todayNotifs::remove)
  }

  private val Class.identifier
    get() = ClassIdentifier(subject, type)

  private data class ClassIdentifier(
    val subject: String,
    val classType: Type,
  )

  private data class ScheduledNotification(
    val notifyAt: LocalDateTime,
    val future: Future<*>,
  )

  private fun ScheduledNotification.canBeInProcess(atTime: LocalDateTime): Boolean {
    val secondsToNotify = Duration.between(atTime, notifyAt).toSeconds()
    return secondsToNotify.absoluteValue <= NOTIFICATION_EPS_SEC
  }
}
