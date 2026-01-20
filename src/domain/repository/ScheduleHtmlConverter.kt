package domain.repository

import domain.Schedule

interface ScheduleHtmlConverter {
  fun fromHtml(html: String): Schedule
  fun toHtml(schedule: Schedule): String
}
