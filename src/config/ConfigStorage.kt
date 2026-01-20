package config

import com.fasterxml.jackson.databind.JsonNode

interface ConfigStorage<T : ConfigKey, R : Config<T>> {
  fun update(newConfig: R)
  fun get(): R
  fun configJson(): JsonNode
}
