package domain.repository

data class RawSchedule(
  val json: String,
  val name: String,
  val checkSum: Long,
)
