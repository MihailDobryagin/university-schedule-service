import ManageCommand.STOP
import domain.NotificationSchedulerJob
import domain.ScheduleMonitoring
import domain.config.ConfigLoader
import domain.config.ShutdownConfigProcessor
import domain.health.HealthCheckerJob
import kotlinx.coroutines.runBlocking
import ktor.HttpServer
import modules.ControllersModule
import modules.CryptoModule
import modules.ProdModule
import modules.TelegramModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject
import org.koin.logger.slf4jLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.SystemUtils.env
import utils.SystemUtils.envOrNull
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val runtime: Runtime = Runtime.getRuntime()
private val logger: Logger = LoggerFactory.getLogger("Launcher")

fun main() {
  logger.info("Service is starting...")

  startKoin {
    slf4jLogger()
    val module = (TelegramModule + ControllersModule + ProdModule)
      .plusIf({ CryptoModule }) { envOrNull("USE_CRYPTO").toBoolean() }
    modules(module)
  }

  try {
    loadConfig()
    startManageCommandsProcessing()
    startHttpServer()
    startMonitoring()
    startJobs()
//    addShutdownHooks()
  } catch (e: Exception) {
    logger.error("Error during start-up", e)
    exitProcess(1)
  }
}

private fun startHttpServer() {
  val port = env("PORT").toInt()
  val httpServer = HttpServer(port)

  runtime.addShutdownHook(Thread {
    httpServer.shutdown()
  })

  httpServer.start()
}

private fun startMonitoring() {
  val scheduleMonitoring by inject<ScheduleMonitoring>(ScheduleMonitoring::class.java)

  scheduleMonitoring.start()
}

private fun loadConfig() {
  val configLoader by inject<ConfigLoader>(ConfigLoader::class.java)
  runBlocking { configLoader.load() }
}

private fun startManageCommandsProcessing() {
  val manageCommands by inject<Queue<ManageCommand>>(Queue::class.java, named("manageCommands"))

  thread {
    while (true) {
      sleep(1000)
      val command = manageCommands.poll() ?: continue
      if (command == STOP) exitProcess(0)
      else logger.error("Unknown command '$command'")
    }
  }
}

private fun startJobs() {
  val healthCheckerJob by inject<NotificationSchedulerJob>(NotificationSchedulerJob::class.java)
  val notificationSchedulerJob by inject<HealthCheckerJob>(HealthCheckerJob::class.java)

  healthCheckerJob.start()
  notificationSchedulerJob.start()
}

private fun addShutdownHooks() {
  val shutdownConfigProcessor by inject<ShutdownConfigProcessor>(ShutdownConfigProcessor::class.java)
  runtime.addShutdownHook(Thread {
    runBlocking { shutdownConfigProcessor.process() }
  })
}

private fun List<Module>.plusIf(moduleProvider: () -> Module, predicate: () -> Boolean) =
  if (predicate()) plus(moduleProvider()) else this
