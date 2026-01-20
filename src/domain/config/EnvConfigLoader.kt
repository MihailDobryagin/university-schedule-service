package domain.config

import config.ConfigService

class EnvConfigLoader(
  private val configService: ConfigService,
) : ConfigLoader {
  override suspend fun load() {

  }
}
