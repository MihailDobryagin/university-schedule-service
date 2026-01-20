package domain.repository

import domain.Schedule

interface ScheduleRepository {
  suspend fun checkAndUpdateSchedule(): UpdateScheduleResult
  suspend fun actualRawSchedule(): RawSchedule?
  suspend fun actualHtmlSchedule(): HtmlSchedule?
  suspend fun actualSchedule(): Schedule?

  sealed interface UpdateScheduleResult {
    object NoChangesResult : UpdateScheduleResult
    object NoScheduleResult : UpdateScheduleResult
    data class HasNewScheduleResult(val html: HtmlSchedule, val number: Int) : UpdateScheduleResult
  }
}
