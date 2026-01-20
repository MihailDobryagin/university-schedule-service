package domain

interface NotificationScheduler {
  suspend fun reSchedule()
}
