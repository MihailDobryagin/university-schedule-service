package domain.repository

import domain.Schedule
import domain.Schedule.Class
import domain.repository.ScheduleRepository.UpdateScheduleResult
import domain.repository.client.ScheduleClientFactory
import domain.repository.client.SuccessReceiveScheduleResult
import domain.repository.client.SuccessReceiveSchedulesResult
import org.jsoup.Jsoup
import utils.StringUtils.checkSum
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

class ScheduleRepositoryImpl(
  scheduleClientFactory: ScheduleClientFactory,
  private val skipClassesSettingsRepository: SkipClassesSettingsRepository,
  private val scheduleHtmlConverter: ScheduleHtmlConverter,
  private val scheduleJsonConverter: ScheduleJsonConverter,
  schedulesPackagePath: String,
) : ScheduleRepository {
  private val scheduleClient = scheduleClientFactory.client()

  private val schedulesPackage = File(schedulesPackagePath).apply {
    if (!isDirectory) throw IllegalStateException("Provided schedules package path is not a package")
  }
  private var previousSchedule: Schedule? = schedulesPackage.listFiles().maxByOrNull(File::lastModified)
    ?.readText()
    ?.let(scheduleJsonConverter::fromJson)

  override suspend fun checkAndUpdateSchedule(): UpdateScheduleResult {
    val fullCurrentSchedule = run {
      val html = (scheduleClient.schedule() as? SuccessReceiveScheduleResult)?.schedule
        ?: return UpdateScheduleResult.NoScheduleResult
      val additionalHtmls = (scheduleClient.additionalSchedules() as? SuccessReceiveSchedulesResult)?.schedules
        ?: return UpdateScheduleResult.NoScheduleResult

      val currentSchedule = htmlToSchedule(html) ?: return UpdateScheduleResult.NoScheduleResult
      val additionalSchedules = additionalHtmls.map(::htmlToSchedule)
      var fullCurrentSchedule = currentSchedule
      additionalSchedules.forEach { fullCurrentSchedule = fullCurrentSchedule + it }

      val skipClassesSettings = skipClassesSettingsRepository.settings()
      val scheduleWithoutFilteredClasses = fullCurrentSchedule.classesByDay
        .mapValues { (date, classes) ->
          classes.filter { lesson ->
            val settingToCheck = SkipClassSetting(
              date = date,
              startsAt = lesson.period?.startAt,
              subject = lesson.subject,
            )
            settingToCheck !in skipClassesSettings
          }
        }
        .filterValues { it.isNotEmpty() }
        .let { Schedule(it) }

      scheduleWithoutFilteredClasses
    }

    if (fullCurrentSchedule == previousSchedule) return UpdateScheduleResult.NoChangesResult

    val newScheduleNumber = addNewSchedule(fullCurrentSchedule)

    return UpdateScheduleResult.HasNewScheduleResult(
      scheduleHtmlConverter.toHtml(fullCurrentSchedule).let { HtmlSchedule(it, it.checkSum) },
      newScheduleNumber
    )
  }

  override suspend fun actualRawSchedule(): RawSchedule? = actualRawScheduleInternal()

  override suspend fun actualHtmlSchedule(): HtmlSchedule? {
    val schedule = actualScheduleInternal() ?: return null
    val html = scheduleHtmlConverter.toHtml(schedule)
    return HtmlSchedule(html, html.checkSum)
  }

  override suspend fun actualSchedule(): Schedule? = actualScheduleInternal()

  private operator fun Schedule.plus(additionalSchedule: Schedule?): Schedule {
    if (additionalSchedule == null) return this


    val resultClassesByDayWithUnknownPeriod = mutableMapOf<LocalDate, MutableMap<String, Class>>()
    val resultClassesByDayWithKnownPeriod = mutableMapOf<LocalDate, MutableMap<LocalTime, Class>>()
    classesByDay.forEach { date, classes ->
      val classesWithUnknownPeriod = mutableMapOf<String, Class>()
      val classesWithKnownPeriod = mutableMapOf<LocalTime, Class>()
      classes.forEach { lesson ->
        if (lesson.period == null)
          classesWithUnknownPeriod[lesson.subject] = lesson
        else
          classesWithKnownPeriod[lesson.period.startAt] = lesson
      }
      resultClassesByDayWithUnknownPeriod[date] = classesWithUnknownPeriod
      resultClassesByDayWithKnownPeriod[date] = classesWithKnownPeriod
    }

    additionalSchedule.classesByDay.forEach { date, additionalClasses ->
      val classesWithKnownPeriod = resultClassesByDayWithKnownPeriod.getOrPut(date) { mutableMapOf() }
      val classesWithUnknownPeriod = resultClassesByDayWithUnknownPeriod.getOrPut(date) { mutableMapOf() }
      additionalClasses.forEach { lesson ->
        if (lesson.period == null)
          classesWithUnknownPeriod[lesson.subject] = lesson
        else
          classesWithKnownPeriod[lesson.period.startAt] = lesson
      }
    }

    val mergedClasses = buildMap {
      (resultClassesByDayWithUnknownPeriod.keys + resultClassesByDayWithKnownPeriod.keys).forEach { date ->
        val classesForDate = resultClassesByDayWithKnownPeriod.getOrDefault(date, emptyMap()).values +
          resultClassesByDayWithUnknownPeriod.getOrDefault(date, emptyMap()).values
        put(date, classesForDate)
      }
    }

    return Schedule(mergedClasses)
  }

  private fun htmlToSchedule(html: String): Schedule? {
    val table = Jsoup.parse(html).selectFirst("table.tablepress") ?: return null
    val normalizedHtml = table.clearAttributes().outerHtml()
    return scheduleHtmlConverter.fromHtml(normalizedHtml)
  }

  private fun actualScheduleInternal(): Schedule? {
    val json = actualRawScheduleInternal()?.json ?: return null
    return scheduleJsonConverter.fromJson(json)
  }

  private fun addNewSchedule(schedule: Schedule): Int {
    previousSchedule = schedule
    val scheduleNumber = schedulesPackage.listFiles().size + 1
    val newScheduleFile = scheduleFile(scheduleNumber).also { it.createNewFile() }
    val json = scheduleJsonConverter.toJson(schedule)
    newScheduleFile.writeText(json)
    return scheduleNumber
  }

  private fun actualRawScheduleInternal() =
    scheduleFile(schedulesPackage.listFiles().size).let { if (it.exists()) it else null }?.let {
      val json = it.readBytes().decodeToString()
      val checkSum = json.checkSum
      val name = it.nameWithoutExtension
      RawSchedule(json = json, name = name, checkSum = checkSum)
    }

  private fun scheduleFileName(scheduleNumber: Int) = "schedule_$scheduleNumber.json"

  private fun scheduleFile(scheduleNumber: Int) =
    File(schedulesPackage.absolutePath + '/' + scheduleFileName(scheduleNumber))
}
