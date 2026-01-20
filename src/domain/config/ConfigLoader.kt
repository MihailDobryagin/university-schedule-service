package domain.config

interface ConfigLoader {
  suspend fun load()
}
