package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
  private val dateIsoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val dotsFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  fun LocalDate.toIso(): String = dateIsoFormatter.format(this)
  fun String.parseIso(): LocalDate = LocalDate.parse(this, dateIsoFormatter)

  fun LocalDate.formatWithDots(): String = dotsFormatter.format(this)
}
