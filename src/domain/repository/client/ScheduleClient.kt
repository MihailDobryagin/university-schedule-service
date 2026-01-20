package domain.repository.client

interface ScheduleClient {
  suspend fun schedule(): ReceiveScheduleResult
  suspend fun additionalSchedules(): ReceiveSchedulesResult
}
