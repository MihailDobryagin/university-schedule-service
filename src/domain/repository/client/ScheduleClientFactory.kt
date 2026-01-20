package domain.repository.client

interface ScheduleClientFactory {
  fun client(): ScheduleClient
}
