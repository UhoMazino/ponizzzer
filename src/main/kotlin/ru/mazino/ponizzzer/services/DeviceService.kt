package ru.mazino.ponizzzer.services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.mazino.ponizzzer.OLT
import ru.mazino.ponizzzer.models.Device

class DeviceService(private val client: HttpClient, private val baseURL: String, private val token: String) {
    private val json = Json { ignoreUnknownKeys = true }

    val devices: List<Device> = runBlocking {
        json.decodeFromString<Response>(
            client.get("http://${baseURL}/dcim/devices/") {
                parameter("role", "OLT")
                parameter("limit", "3")
                parameter("manufaturer", "RAISECOM")
                parameter("manufaturer", "GATERAY")
                parameter("model", "iscom5508")
                parameter("model", "gr-ep-olt1-8")
                headers {
                    set(HttpHeaders.Authorization, "Token $token")
                }
            }
        )
            .results
            .map(::OLT)
    }

    fun discover() = runBlocking {
        devices.forEach { launch { it.discover() } }
    }

}

@Serializable
data class Response(val results: List<NetboxDevice>)

@Serializable
data class NetboxDevice(
    val name: String,
    private val custom_fields: NetboxDeviceFields,
    private val device_type: NetboxDeviceType,
    private val primary_ip: NetboxDeviceAddress,
    private val status: NetboxDeviceStatus,
    private val comments: String
) {
    val model by device_type::slug
    val vendor by device_type.manufacturer::slug
    val community by custom_fields.`SNMP community`::label
    val host = primary_ip.address.substringBefore('/')
    val active = status.value == "active"

    var hardwareVersion: String? = null
        private set
    var softwareVersion: String? = null
        private set
    var mac: String? = null
        private set

    init {
        comments.split("\r\n")
            .forEach {
                when {
                    it.contains("MAC", true) -> mac = it.substringAfter(":").trim()
                    it.contains("Software Version", true) -> softwareVersion = it.substringAfter(":").trim()
                    it.contains("Hardware Version", true) -> hardwareVersion = it.substringAfter(":").trim()
                }
            }
    }
}

@Serializable
data class NetboxDeviceType(val slug: String, val manufacturer: NetboxDeviceVendor)

@Serializable
data class NetboxDeviceVendor(val slug: String)

@Serializable
data class NetboxDeviceAddress(val address: String)

@Serializable
data class NetboxDeviceStatus(val value: String)

@Serializable
data class NetboxDeviceFields(val `SNMP community`: NetboxDeviceCommunity)

@Serializable
data class NetboxDeviceCommunity(val label: String)