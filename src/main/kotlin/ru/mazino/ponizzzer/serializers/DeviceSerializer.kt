package ru.mazino.ponizzzer.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.mazino.ponizzzer.OLT

object DeviceSerializer : KSerializer<OLT> {
    override fun deserialize(decoder: Decoder): OLT {
        TODO("Not yet implemented")
    }

    override val descriptor = DeviceSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: OLT) {
        encoder.encodeSerializableValue(
            DeviceSurrogate.serializer(),
            DeviceSurrogate(value.host, value.name, value.vendor, value.model, value.active, value.softwareVersion)
        )
    }
}

@Serializable
@SerialName("OLT")
private data class DeviceSurrogate(
    val host: String,
    val name: String,
    val vendor: String,
    val model: String,
    val active: Boolean,
    val softwareVersion: String?
)
