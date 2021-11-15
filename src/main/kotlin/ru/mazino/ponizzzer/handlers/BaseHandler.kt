package ru.mazino.ponizzzer.handlers

import ru.mazino.ponizzzer.models.DeviceHandler
import ru.mazino.ponizzzer.services.SnmpContext

abstract class BaseHandler(protected val snmp: SnmpContext) : DeviceHandler {
    protected val list: Map<String, String> = HashMap()
}