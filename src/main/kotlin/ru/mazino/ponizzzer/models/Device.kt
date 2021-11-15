package ru.mazino.ponizzzer.models

import org.koin.core.component.KoinComponent

interface Device : KoinComponent {
    val name: String
    val host: String
    val vendor: String
    val model: String
    val active: Boolean
    val softwareVersion: String?

    fun discover()
}