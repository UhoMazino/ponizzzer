package ru.mazino.ponizzzer.services

import org.snmp4j.CommunityTarget
import org.snmp4j.PDU
import org.snmp4j.Snmp
import org.snmp4j.event.ResponseEvent
import org.snmp4j.event.ResponseListener
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.*
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.DefaultPDUFactory
import org.snmp4j.util.TreeEvent
import org.snmp4j.util.TreeListener
import org.snmp4j.util.TreeUtils


class SnmpContext(val host: String, community: String, timeout: Long, retries: Int, port: Int) {
    private val target = CommunityTarget(UdpAddress("$host/$port"), OctetString(community))
        .also {
            it.timeout = timeout
            it.retries = retries
            it.version = SnmpConstants.version2c
        }
    private val snmp = Snmp(DefaultUdpTransportMapping()).apply { listen() }

/*    private fun target(address: String, community: String): CommunityTarget<Address> {
        return CommunityTarget<Address>().also {
            it.address = UdpAddress("$address/$port")
            it.community = OctetString(community)
            it.timeout = timeout
            it.retries = retries
            it.version = SnmpConstants.version2c
        }
    }*/


    private fun listener(callback: (e: ResponseEvent<out Address?>) -> Unit): ResponseListener {
        return object : ResponseListener {
            override fun <A : Address?> onResponse(e: ResponseEvent<A?>) {
                callback(e)
                snmp.cancel(e.request, this)
            }

        }
    }

    private fun treeListener(
        callback: (e: TreeEvent?) -> Boolean,
        done: ((e: TreeEvent?) -> Unit)? = null
    ): TreeListener {
        return object : TreeListener {
            private var finished: Boolean = false

            override fun isFinished(): Boolean = finished
            override fun next(e: TreeEvent?): Boolean = callback(e)
            override fun finished(e: TreeEvent?) {
                if (e?.variableBindings?.size!! > 0) next(e)
                if (e.isError) println(e.errorMessage)
                finished = true
                done?.invoke(e)
            }

        }
    }


    private fun request(oid: String): PDU {
        return PDU().apply {
            type = PDU.GET
            add(VariableBinding(OID(oid)))
        }
    }


    fun get(oid: String, callback: (e: ResponseEvent<out Address>) -> Unit) {
        snmp.get(request(oid), target, null, listener(callback))
    }

    fun subtree(
        oid: String,
        callback: (e: TreeEvent?) -> Boolean,
        done: ((e: TreeEvent?) -> Unit)? = null
    ) {
        TreeUtils(snmp, DefaultPDUFactory()).apply {
            getSubtree(target, OID(oid), null, treeListener(callback, done))
        }
    }

}