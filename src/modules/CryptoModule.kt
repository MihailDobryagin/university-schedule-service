package modules

import com.mdobryagin.crypto.Crypto
import com.mdobryagin.crypto.CryptoImpl
import com.mdobryagin.crypto.Secrets
import com.mdobryagin.tgbots.blockingcommands.BlockingCommandStorage
import domain.admin.crypto.CryptoTelegramMessagesResolver
import domain.crypto.SecretsServiceImpl
import domain.admin.crypto.SecretsResponseHandlerImpl
import io.ktor.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import modules.ReceiveSecretsWay.FILE
import modules.ReceiveSecretsWay.TELEGRAM
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import telegram.TelegramBot
import utils.SystemUtils.env
import utils.SystemUtils.envOrNull
import java.io.File

val CryptoModule = module {
  single(named("cryptoSecrets")) {
    val secrets = readSecrets()
    if (envOrNull("CLEANUP_CRYPTO_SECRETS").toBoolean()) cleanupSecrets()
    secrets
  }

  single<Crypto> { CryptoImpl(get(named("cryptoSecrets"))) }
}

private fun readSecrets(): Secrets = when (env("RECEIVE_CRYPTO_SECRETS").let(ReceiveSecretsWay::valueOf)) {
  FILE -> readSecretsFromFile()
  TELEGRAM -> readSecretsFromTelegram()
}

private fun cleanupSecrets() = File(env("CRYPTO_SECRETS_FILE")).delete()
private fun readSecretsFromFile() = File(env("CRYPTO_SECRETS_FILE")).bufferedReader().use {
  val transformation = it.readLine().decodeBase64String()
  val ivSpec = it.readLine().decodeBase64Bytes()
  val key = it.readLine().decodeBase64Bytes()
  Secrets(transformation, ivSpec, key)
}

private fun readSecretsFromTelegram(): Secrets {
  val bot by inject<TelegramBot>(TelegramBot::class.java)
  val commandStore by inject<BlockingCommandStorage>(BlockingCommandStorage::class.java)

  val secretsChannel = Channel<List<String>>(1)
  val handler = SecretsResponseHandlerImpl(secretsChannel, commandStore)
  CryptoTelegramMessagesResolver(commandStore, handler).also(bot::registerResolver)

  val service = SecretsServiceImpl(bot, env("TELEGRAM_CHAT_ID").toLong(), commandStore, secretsChannel)

  val secrets = runBlocking { service.requestSecrets() }.let {
    Secrets(it[0].decodeBase64String(), it[1].decodeBase64Bytes(), it[2].decodeBase64Bytes())
  }
  return secrets
}

private enum class ReceiveSecretsWay {
  FILE,
  TELEGRAM,
}
