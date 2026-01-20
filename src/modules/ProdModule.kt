package modules

import ManageCommand
import com.mdobryagin.crypto.Crypto
import com.mdobryagin.mails.MessageService
import com.mdobryagin.mails.MessageServiceImpl
import com.mdobryagin.mails.SmtpServer
import com.mdobryagin.tgbots.callbacks.CallbackDataConverter
import com.mdobryagin.tgbots.callbacks.CallbackDataConverterImpl
import config.ConfigService
import config.ConfigServiceImpl
import domain.*
import domain.admin.telegram.*
import domain.config.ConfigLoader
import domain.config.EnvConfigLoader
import domain.config.ShutdownConfigProcessor
import domain.config.admin.AdminConfigKey
import domain.config.admin.AdminConfigStorage
import domain.health.HealthCheckerJob
import domain.repository.*
import domain.repository.client.*
import domain.telegram.*
import domain.admin.telegram.AdminTelegramMessagesResolver
import kotlinx.coroutines.channels.Channel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import telegram.TelegramBot
import utils.SystemUtils.env
import utils.SystemUtils.envOrNull
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

val ProdModule = module {
  single<Queue<ManageCommand>>(named("manageCommands")) { ConcurrentLinkedQueue() }
  single<AppManagingService> { AppManagingServiceImpl(get(named("manageCommands"))) }

  single<MessageService> {
    MessageServiceImpl(
      SmtpServer(
        host = env("SMTP_HOST").decoded(),
        user = env("SMTP_USER").decoded(),
        password = env("SMTP_PASSWORD").decoded(),
      )
    )
  }

  single<MonitoringService> { MonitoringServiceImpl(env("MONITORING_EMAILS").split(";"), get()) }
  single { HealthCheckerJob(env("HEALTH_CHECK_DELAY_SEC").toLong(), get()) }

  single {
    RemoteScheduleClient(
      monitoringService = get(),
      baseUrl = env("SCHEDULE_URL"),
      mainPath = env("MAIN_SCHEDULE_PATH"),
      additionalPaths = envOrNull("ADDITIONAL_SCHEDULE_PATH")?.split(";") ?: emptyList(),
    )
  }
  single { FileScheduleClient(env("SCHEDULE_FILE_PATH")) }
  single<ScheduleClientFactory> {
    ScheduleClientFactoryImpl(
      clientType = env("SCHEDULE_CLIENT_TYPE").let(ScheduleClientType::valueOf),
      remoteClientProvider = { get<RemoteScheduleClient>() },
      fileClientProvider = { get<FileScheduleClient>() },
    )
  }

  single<SkipClassesSettingsRepository> { SkipClassesSettingsRepositoryImpl(env("SKIP_CLASSES_SETTINGS_FILE_PATH")) }
  single<ScheduleRepository> {
    ScheduleRepositoryImpl(
      get(), get(), ScheduleHtmlConverterImpl(), ScheduleJsonConverterImpl(),
      schedulesPackagePath = env("SCHEDULES_PACKAGE"),
    )
  }

  single<ScheduleService> {
    ScheduleServiceImpl(
      subscribersEmails = env("SUBSCRIBER_EMAILS").split(";"),
      subscribersTelegramChatIds = env("SUBSCRIBER_TELEGRAM_CHATS").split(";").map { it.toLong() },
      get(), get(), get(), get(),
    )
  }

  config()
  tg()

  single { ScheduleMonitoring(get(), get(), env("MONITORING_DELAY_SEC").toLong()) }
  single<NotificationService> {
    NotificationServiceImpl(get(), env("SUBSCRIBER_TELEGRAM_CHATS").split(";").map { it.toLong() }.toSet(), get())
  }
  single<NotificationScheduler> { NotificationSchedulerImpl(get(), get()) }
  single { NotificationSchedulerJob(get(), env("RESCHEDULE_DELAY_SEC").toLong()) }
}

private fun Module.config() {
  single<ConfigService> { ConfigServiceImpl() }
  single(createdAtStart = true) {
    AdminConfigStorage().also { get<ConfigService>().registerStorage(AdminConfigKey, it) }
  }
  single { ShutdownConfigProcessor(env("TELEGRAM_CHAT_ID").toLong(), get(), get()) }
  single(named("loadConfigsChannel")) { Channel<String>(1) }
  single<ConfigLoader> {
//    TelegramConfigLoader(env("TELEGRAM_CHAT_ID").toLong(), get(), get(), get(named("loadConfigsChannel")), get())
    EnvConfigLoader(get())
  }
}

private fun Module.tg() {
  val telegramBot by inject<TelegramBot>(TelegramBot::class.java)

  single<CallbackDataConverter> { CallbackDataConverterImpl() }
  single<HealthMessageHandler> { HealthMessageHandlerImpl(get()) }
  single<RawScheduleRequestHandler> { RawScheduleRequestHandlerImpl(get(), get()) }
  single<ClassesMessageComposer> { ClassesMessageComposerImpl() }
  single<ClassesRequestHandler> { ClassesRequestHandlerImpl(get(), get(), get()) }
  single<ScheduleKeyboardComposer> { ScheduleKeyboardComposerImpl(get()) }
  single<ScheduleCalendarRequestHandler> { ScheduleCalendarRequestHandlerImpl(get(), get(), get(), get()) }
  single<ClassesFromMenuRequestHandler> { ClassesFromMenuRequestHandlerImpl(get(), get(), get(), get()) }
  single(createdAtStart = true) {
    DomainTelegramMessagesResolver(get(), get(), get(), get(), get(), get()).also(telegramBot::registerResolver)
  }

  single<StopMessageHandler> { StopMessageHandlerImpl(get(named("manageCommands"))) }
  single<WhiteListRepository>(createdAtStart = true) {
    WhiteListRepositoryImpl().also {
      telegramBot.registerAllowedUsersProvider { it.whitelistUsers() }
    }
  }
  single<WhiteListMessageHandler> { WhiteListMessageHandlerImpl(telegramBot, get(), get()) }
  single<LoadConfigResponseHandler> { LoadConfigResponseHandlerImpl(get(named("loadConfigsChannel")), get()) }
  single(createdAtStart = true) {
    AdminTelegramMessagesResolver(env("TELEGRAM_ADMIN"), get(), get(), get(), get()).also(telegramBot::registerResolver)
  }
}

private fun String.decoded(): String = if (envOrNull("USE_CRYPTO").toBoolean()) {
  val crypto by inject<Crypto>(Crypto::class.java)
  crypto.decode(this)
} else this
