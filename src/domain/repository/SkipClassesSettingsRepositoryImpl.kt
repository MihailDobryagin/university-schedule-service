package domain.repository

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import utils.FileUtils.file
import java.time.LocalDate
import java.time.LocalTime

class SkipClassesSettingsRepositoryImpl(settingsFilePath: String) : SkipClassesSettingsRepository {
  private val jsonMapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .build()

  private val file = file(settingsFilePath)
  private val settings = file.readText()
    .let { jsonMapper.readValue<List<StoredSetting>>(it) }
    .map {
      SkipClassSetting(
        date = it.date,
        startsAt = it.startsAt,
        subject = it.subject,
      )
    }
    .toSet()

  override suspend fun settings(): Set<SkipClassSetting> = settings

  private data class StoredSetting(
    @JsonProperty("date")
    val date: LocalDate,
    @JsonProperty("startsAt")
    val startsAt: LocalTime?,
    @JsonProperty("subject")
    val subject: String,
  )
}
