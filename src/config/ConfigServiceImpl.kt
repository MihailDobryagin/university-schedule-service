package config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConfigServiceImpl(
  private val logger: Logger = LoggerFactory.getLogger(ConfigServiceImpl::class.java),
) : ConfigService {
  private val jsonMapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .build()

  private val configStorages = mutableMapOf<ConfigKey, ConfigStorage<out ConfigKey, *>>()

  override fun <T : ConfigKey, R : ConfigStorage<T, *>> registerStorage(key: T, configStorage: R) {
    if (configStorages.containsKey(key)) {
      throw IllegalArgumentException("Config storage '${key.name}' is already registered")
    }

    configStorages[key] = configStorage
  }

  override fun <T : ConfigKey> updateConfig(key: T, config: Config<T>) {
    val storage = (configStorages[key] ?: run {
      logger.error("No config for '${key.name}'")
      return
    }) as ConfigStorage<T, Config<T>>

    storage.update(config)
  }

  override fun configsJson(): JsonNode = configStorages
    .map { (key, value) -> ConfigWrapper(key, value.configJson()) }
    .let(jsonMapper::valueToTree)

  class ConfigWrapper<T : ConfigKey> private constructor(
    val key: String,
    val config: JsonNode,
  ) {
    constructor(key: T, config: JsonNode) : this(key.name, config)
  }
}
