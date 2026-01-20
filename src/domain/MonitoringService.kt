package domain

interface MonitoringService {
  fun submitError(error: String)
  fun submitError(error: String, exception: Throwable)
  fun submitHeartBeat()
}
