package domain

import domain.Schedule.Class
import domain.repository.HtmlSchedule
import domain.repository.RawSchedule
import java.time.LocalDate

interface ScheduleService {
  suspend fun checkSchedule()
  suspend fun rawSchedule(): RawSchedule?
  suspend fun htmlSchedule(): HtmlSchedule?
  suspend fun schedule(): Schedule?
  suspend fun scheduleForDate(date: LocalDate): List<Class>?
}
