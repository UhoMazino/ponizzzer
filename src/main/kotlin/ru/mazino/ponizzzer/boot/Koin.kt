package ru.mazino.ponizzzer.boot

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.fileProperties
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger
import ru.mazino.ponizzzer.loadScripts
import ru.mazino.ponizzzer.services.DeviceService
import ru.mazino.ponizzzer.services.SnmpContext


fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        fileProperties()
        launch {
            loadScripts()
        }
        modules(module {
            single(createdAtStart = true) { HttpClient(CIO); }
            single(createdAtStart = true) {
                DeviceService(get(), getProperty("netbox.baseURL"), getProperty("netbox.token"))
            }
            factory { (host: String, community: String) ->
                SnmpContext(
                    host,
                    community,
                    getProperty("timeout", "15000").toLong(),
                    getProperty("retries", "3").toInt(),
                    getProperty("port", "161").toInt()
                )
            }
        })
    }
}

