package ru.mazino.ponizzzer.events

import ru.mazino.ponizzzer.ONU
import kotlin.reflect.KClass

object NewOnu : Event {
    private val listeners: MutableMap<KClass<out NewOnuListener>, NewOnuListener> = HashMap()

    fun on(handler: NewOnuListener) {
        listeners[handler::class] = handler
    }

    fun off(handler: NewOnuListener) {
        listeners.remove(handler::class)
    }

    fun emit(payload: Pair<String, Map<String, ONU>>) {
        listeners.forEach { it.value.fire(payload) }
    }
}

fun interface NewOnuListener {
    fun fire(payload: Pair<String, Map<String, ONU>>)
}