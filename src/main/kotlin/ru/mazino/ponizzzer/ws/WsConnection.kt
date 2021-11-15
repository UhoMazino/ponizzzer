package ru.mazino.ponizzzer.ws

import io.ktor.websocket.*
import java.util.concurrent.atomic.AtomicInteger

class WsConnection(val session: DefaultWebSocketServerSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user[${lastId.getAndIncrement()}]"
}