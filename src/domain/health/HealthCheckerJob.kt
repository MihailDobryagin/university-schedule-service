package domain.health

import domain.MonitoringService
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class HealthCheckerJob(
  private val reScheduleDelaySec: Long,
  private val monitoringService: MonitoringService,
  private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1),
) {
  fun start() {
    schedule(executeImmediately = true)
  }

  private fun schedule(executeImmediately: Boolean) {
    scheduledExecutor.schedule({
      schedule(executeImmediately = false)
      runBlocking { monitoringService.submitHeartBeat() }
    }, if (executeImmediately) 0 else reScheduleDelaySec, TimeUnit.SECONDS)
  }
}

