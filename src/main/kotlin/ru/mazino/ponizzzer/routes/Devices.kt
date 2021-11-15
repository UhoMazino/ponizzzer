package ru.mazino.ponizzzer.routes

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import ru.mazino.ponizzzer.events.NewOnu
import ru.mazino.ponizzzer.events.NewOnuListener
import ru.mazino.ponizzzer.services.DeviceService
import ru.mazino.ponizzzer.ws.WsConnection
import java.util.*

fun Route.devicesRoutes() {
    val deviceService: DeviceService = get()

    get("/devices") {
        call.respond(deviceService.devices)

    }

    val connections = Collections.synchronizedSet<WsConnection?>(LinkedHashSet())
    webSocket("/devices") {
        val handler = NewOnuListener {
            val msg = Json.encodeToString(it)
            launch {
                send(msg)
            }
        }
        val connection = WsConnection(this)
        connections += connection
        try {
            NewOnu.on(handler)
            for (frame in incoming) {
                frame as Frame.Text
                if (frame.readText() == "discover") {
                    deviceService.discover()
                }
            }
        } catch (ex: Exception) {
            println(ex)
        } finally {
            NewOnu.off(handler)
            connections -= connection
        }


    }
}