package domain.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import config.Config
import config.ConfigKey
import config.ConfigService
import domain.config.ConfigKeys.ADMIN
import domain.config.admin.AdminConfig
import domain.config.admin.AdminConfigKey
import domain.config.admin.ReceiveConfigCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import telegram.TelegramBot

class TelegramConfigLoader(
  private val adminChatId: Long,
  private val bot: TelegramBot,
  private val blockingCommandStorage: BlockingCommandStorage,
  private val configsChannel: Channel<String>,
  private val configService: ConfigService,
  private val logger: Logger = LoggerFactory.getLogger(TelegramConfigLoader::class.java),
) : ConfigLoader {
  private val jsonMapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .build()

  override suspend fun load() {
    val configs = configs()
    configs.forEach(::upload)
  }

  private suspend fun configs(): List<ConfigWrapper<*>> {
    while (!blockingCommandStorage.store(adminChatId, ReceiveConfigCommand)) {
      logger.warn("Another command is waiting for TELEGRAM. Wait...")
      delay(500)
    }

    bot.sendMessage(adminChatId, "Request for configs...")
    val configsJson = configsChannel.receive()
    configsChannel.close()
    bot.sendMessage(adminChatId, "Configs received.")
    return configsJson.parseConfigs()
  }

  private fun upload(configWrapper: ConfigWrapper<*>) {
    configService.updateConfig(configWrapper.key, configWrapper.config as Config<ConfigKey>)
  }

  private fun String.parseConfigs(): List<ConfigWrapper<*>> {
    return jsonMapper.readTree(this)
      .map { node ->
        val key = node["key"].asText().let {
          when (it) {
            ADMIN.name -> AdminConfigKey
            else -> throw IllegalArgumentException()
          }
        }
        val config = node["config"].parse(key)
        ConfigWrapper(key, config)
      }
  }

  private fun <T : ConfigKey> JsonNode.parse(key: T): Config<T> = when (key) {
    is AdminConfigKey -> jsonMapper.treeToValue<AdminConfig>(this)
    else -> throw IllegalArgumentException()
  } as Config<T>

  private data class ConfigWrapper<T : ConfigKey>(
    val key: T,
    val config: Config<T>,
  )
}
