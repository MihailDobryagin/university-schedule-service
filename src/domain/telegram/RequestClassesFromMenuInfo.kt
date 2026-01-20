package domain.telegram

import java.io.Serializable
import java.time.LocalDate

data class RequestClassesFromMenuInfo(
  val calendarMessageId: Int,
  val date: LocalDate,
) : Serializable
