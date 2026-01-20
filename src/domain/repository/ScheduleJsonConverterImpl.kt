package domain.repository

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import domain.Schedule
import domain.Schedule.Class.Type
import utils.LocalDateDeserializer
import utils.LocalDateSerializer
import utils.LocalTimeDeserializer
import utils.LocalTimeSerializer
import java.time.LocalDate
import java.time.LocalTime

class ScheduleJsonConverterImpl : ScheduleJsonConverter {
  private val jsonMapper = jacksonMapperBuilder()
    .enable(ACCEPT_CASE_INSENSITIVE_ENUMS)
    .disable(WRITE_DATES_AS_TIMESTAMPS)
    .disable(FAIL_ON_UNKNOWN_PROPERTIES)
    .addModule(SimpleModule().apply {
      addDeserializer(LocalDate::class.java, LocalDateDeserializer)
      addSerializer(LocalDate::class.java, LocalDateSerializer)
      addDeserializer(LocalTime::class.java, LocalTimeDeserializer)
      addSerializer(LocalTime::class.java, LocalTimeSerializer)
    })
    .build()

  override fun toJson(schedule: Schedule): String {
    val dto = ScheduleDto(
      classesByDay = schedule.classesByDay.map { (date, classes) ->
        ScheduleDto.ClassesByDay(
          date = date,
          classes = classes.map { lesson ->
            ScheduleDto.Class(
              subject = lesson.subject,
              teacher = lesson.teacher,
              startAt = lesson.period?.startAt,
              endAt = lesson.period?.endAt,
              group = lesson.group,
              type = lesson.type,
              classroom = lesson.classroom,
            )
          }
        )
      }
    )
    return jsonMapper.writeValueAsString(dto)
  }

  override fun fromJson(json: String): Schedule {
    val dto = jsonMapper.readValue<ScheduleDto>(json)
    return Schedule(
      dto.classesByDay.associate { (date, classes) ->
        date to classes.map { lesson ->
          Schedule.Class(
            subject = lesson.subject,
            teacher = lesson.teacher,
            date = date,
            period = lesson.startAt?.let { Schedule.Class.Period(it, lesson.endAt) },
            group = lesson.group,
            type = lesson.type,
            classroom = lesson.classroom,
          )
        }
      }
    )
  }

  private data class ScheduleDto(
    @JsonProperty("classesByDay")
    val classesByDay: List<ClassesByDay>,
  ) {
    data class ClassesByDay(
      @JsonProperty("date")
      val date: LocalDate,
      @JsonProperty("classes")
      val classes: List<Class>,
    )

    data class Class(
      @JsonProperty("subject")
      val subject: String,
      @JsonProperty("teacher")
      val teacher: String,
      @JsonProperty("startAt")
      val startAt: LocalTime?,
      @JsonProperty("endAt")
      val endAt: LocalTime?,
      @JsonProperty("group")
      val group: String,
      @JsonProperty("type")
      val type: Type,
      @JsonProperty("classroom")
      val classroom: String,
    )
  }
}
