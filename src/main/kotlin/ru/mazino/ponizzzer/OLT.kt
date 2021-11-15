package ru.mazino.ponizzzer

import kotlinx.serialization.Serializable
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import ru.mazino.ponizzzer.models.Device
import ru.mazino.ponizzzer.models.DeviceHandler
import ru.mazino.ponizzzer.serializers.DeviceSerializer
import ru.mazino.ponizzzer.services.NetboxDevice

@Serializable(with = DeviceSerializer::class)
class OLT(private val properties: NetboxDevice) : Device {
    override val name by properties::name
    override val host by properties::host
    override val vendor by properties::vendor
    override val model by properties::model
    override val active by properties::active
    override val softwareVersion by properties::softwareVersion

    private val logger = LoggerFactory.getLogger(OLT::class.java)
    private val handler: DeviceHandler by inject(named("${vendor}/${model}/${softwareVersion}")) {
        parametersOf(properties.host, properties.community)
    }


    override fun discover() {

        handler.discover()
    }
}