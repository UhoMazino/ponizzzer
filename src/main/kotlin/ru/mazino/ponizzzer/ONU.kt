package ru.mazino.ponizzzer

import kotlinx.serialization.Serializable

@Serializable
class ONU(val root: Int, val port: Int, val position: Int)