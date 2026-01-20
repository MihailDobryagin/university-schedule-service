package domain.repository

import domain.Schedule

interface ScheduleJsonConverter {
  fun toJson(schedule: Schedule): String
  fun fromJson(json: String): Schedule
}
