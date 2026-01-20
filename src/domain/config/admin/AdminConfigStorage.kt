package domain.config.admin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import config.ConfigStorage

class AdminConfigStorage : ConfigStorage<AdminConfigKey, AdminConfig> {
  private val config = AdminConfig()
  private val mapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .build()

  override fun update(newConfig: AdminConfig) {
    config.telegramAllowedUsers.apply {
      clear()
      addAll(newConfig.telegramAllowedUsers)
    }
  }

  override fun get(): AdminConfig = config

  override fun configJson(): JsonNode {
    return mapper.valueToTree(config)
  }
}
