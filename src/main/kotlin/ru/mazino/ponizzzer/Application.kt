package ru.mazino.ponizzzer

import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.slf4j.LoggerFactory
import ru.mazino.ponizzzer.boot.*

fun main() {
    embeddedServer(CIO, environment = applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())
        connector {
            port = config.propertyOrNull("ktor.deployment.port")
                ?.getString()?.toInt() ?: 8080
        }
    }).start(true)
}

fun Application.main() {
    configureKoin()
    configureSockets()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()
}


