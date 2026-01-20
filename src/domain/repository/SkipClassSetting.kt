package domain.repository

import java.time.LocalDate
import java.time.LocalTime

data class SkipClassSetting(
  val date: LocalDate,
  val startsAt: LocalTime?,
  val subject: String,
)
