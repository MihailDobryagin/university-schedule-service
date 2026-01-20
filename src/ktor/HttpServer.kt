package ktor

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.component.KoinComponent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpServer(
  port: Int,
  private val logger: Logger = LoggerFactory.getLogger(HttpServer::class.java)
) : KoinComponent {

  private val server = embeddedServer(Netty, port = port) {
    application()
  }

  fun start() {
    server.start(wait = false)
  }

  fun shutdown() {
    server.stop(1000, 5000)
    logger.info("gracefully shutdown")
  }
}
