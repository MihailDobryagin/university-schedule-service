package domain.config.admin

import config.Config

class AdminConfig : Config<AdminConfigKey> {
  val telegramAllowedUsers = mutableListOf<String>()
}
