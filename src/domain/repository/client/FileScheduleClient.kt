package domain.repository.client

import utils.FileUtils.file

class FileScheduleClient(scheduleFilePath: String) : ScheduleClient {
  private val scheduleFile = file(scheduleFilePath)

  private val scheduleHtml = scheduleFile.readText()

  override suspend fun schedule(): ReceiveScheduleResult = if (scheduleHtml.isBlank())
    NoScheduleResult
  else
    SuccessReceiveScheduleResult(scheduleHtml)

  override suspend fun additionalSchedules(): ReceiveSchedulesResult = SuccessReceiveSchedulesResult(emptyList())
}
