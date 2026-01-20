package domain.repository.client

sealed class ReceiveSchedulesResult

object NoSchedulesResult : ReceiveSchedulesResult()
data class SuccessReceiveSchedulesResult(val schedules: List<String>) : ReceiveSchedulesResult()
object ErrorReceiveSchedulesResult : ReceiveSchedulesResult()
