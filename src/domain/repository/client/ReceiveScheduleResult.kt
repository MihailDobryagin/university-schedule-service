package domain.repository.client

sealed class ReceiveScheduleResult

object NoScheduleResult : ReceiveScheduleResult()
data class SuccessReceiveScheduleResult(val schedule: String) : ReceiveScheduleResult()
object ErrorReceiveScheduleResult : ReceiveScheduleResult()
