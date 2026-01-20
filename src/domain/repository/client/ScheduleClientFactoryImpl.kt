package domain.repository.client

import domain.repository.client.ScheduleClientType.FILE
import domain.repository.client.ScheduleClientType.REMOTE

class ScheduleClientFactoryImpl(
  clientType: ScheduleClientType,
  remoteClientProvider: () -> RemoteScheduleClient,
  fileClientProvider: () -> FileScheduleClient,
) : ScheduleClientFactory {
  private val clientProvider = when (clientType) {
    REMOTE -> remoteClientProvider
    FILE -> fileClientProvider
  }

  override fun client(): ScheduleClient = clientProvider()
}
