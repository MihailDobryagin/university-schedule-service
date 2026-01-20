package config

import com.fasterxml.jackson.databind.JsonNode

interface ConfigService {
  fun <A : ConfigKey, C : ConfigStorage<A, *>> registerStorage(key: A, configStorage: C)
  fun <T : ConfigKey> updateConfig(key: T, config: Config<T>)
  fun configsJson(): JsonNode
}
