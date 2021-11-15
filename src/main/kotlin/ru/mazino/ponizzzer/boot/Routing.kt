package ru.mazino.ponizzzer.boot

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.mazino.ponizzzer.routes.devicesRoutes

fun Application.configureRouting() {
    routing {
        get("/") {
            log.info("/ request")
            call.respondText("5")
        }
        devicesRoutes()
    }
}