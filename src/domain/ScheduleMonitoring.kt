package domain

import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ScheduleMonitoring(
  private val scheduleService: ScheduleService,
  private val monitoringService: MonitoringService,
  private val monitoringDelaySec: Long,
  private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1),
) {
  fun start() {
    schedule(executeImmediately = true)
  }

  private fun schedule(executeImmediately: Boolean) {
    scheduledExecutor.schedule({
      try {
        runBlocking { scheduleService.checkSchedule() }
      } catch (e: Exception) {
        monitoringService.submitError("Error during monitoring", e)
      }
      schedule(executeImmediately = false)
    }, if (executeImmediately) 0 else monitoringDelaySec, TimeUnit.SECONDS)
  }
}

