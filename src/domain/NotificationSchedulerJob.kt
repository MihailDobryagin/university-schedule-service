package domain

import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class NotificationSchedulerJob(
  private val notificationScheduler: NotificationScheduler,
  private val reScheduleDelaySec: Long,
  private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1),
) {
  fun start() {
    schedule(executeImmediately = true)
  }

  private fun schedule(executeImmediately: Boolean) {
    scheduledExecutor.schedule({
      schedule(executeImmediately = false)
      runBlocking { notificationScheduler.reSchedule() }
    }, if (executeImmediately) 0 else reScheduleDelaySec, TimeUnit.SECONDS)
  }
}
