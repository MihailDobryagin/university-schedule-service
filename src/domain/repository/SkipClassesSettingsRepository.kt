package domain.repository

interface SkipClassesSettingsRepository {
  suspend fun settings(): Set<SkipClassSetting>
}
