package telegram

import com.mdobryagin.tgbots.ExtendedBotWithIntegration

interface TelegramBot : ExtendedBotWithIntegration {
  fun registerResolver(resolver: TelegramMessageHandlerResolver)
  fun registerAllowedUsersProvider(provider: suspend () -> Set<String>)
}
