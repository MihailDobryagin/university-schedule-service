package domain.repository.client

import domain.MonitoringService
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.TooManyRequests
import java.io.IOException

class RemoteScheduleClient(
  private val monitoringService: MonitoringService,
  private val baseUrl: String,
  private val mainPath: String,
  private val additionalPaths: List<String> = emptyList(),
) : ScheduleClient {
  private val client = HttpClient(OkHttp) {
    followRedirects = true

    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.INFO
    }
  }

  override suspend fun schedule(): ReceiveScheduleResult {
    val url = "$baseUrl/$mainPath/"
    return schedule(url)
  }

  override suspend fun additionalSchedules(): ReceiveSchedulesResult = additionalPaths
    .map { path ->
      val url = "$baseUrl/$path/"
      schedule(url).also { if (it is ErrorReceiveScheduleResult) return ErrorReceiveSchedulesResult }
    }
    .asSequence()
    .mapNotNull { it as? SuccessReceiveScheduleResult }
    .map(SuccessReceiveScheduleResult::schedule)
    .toList()
    .let { if (it.isEmpty()) NoSchedulesResult else SuccessReceiveSchedulesResult(it) }

  private suspend fun schedule(url: String): ReceiveScheduleResult {
    val response = get(url, true)
    val status = response.status

    return when {
      status == OK -> SuccessReceiveScheduleResult(response.bodyAsText())
      status == NotFound -> NoScheduleResult
      status.value >= 500 -> run {
        monitoringService.submitError("${status.value} code from UNIVERSITY ($url)")
        ErrorReceiveScheduleResult
      }

      else -> throw RuntimeException("Unprocessable status code ${response.status} for '$url'")
    }
  }

  private suspend fun get(url: String, waitForTimeout: Boolean): HttpResponse {
    var response: HttpResponse?

    var firstRequest = true
    do {
      if (!firstRequest) Thread.sleep(30000)
      else firstRequest = false
      response = if (!waitForTimeout)
        client.get(url)
      else {
        try {
          client.get(url).let { if (it.status == TooManyRequests) null else it }
        } catch (_: IOException) {
          null
        }
      }
    } while (response == null)

    return response
  }
}
