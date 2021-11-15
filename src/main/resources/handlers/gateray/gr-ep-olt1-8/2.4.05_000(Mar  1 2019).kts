import ru.mazino.ponizzzer.ONU
import ru.mazino.ponizzzer.events.NewOnu
import ru.mazino.ponizzzer.handlers.BaseHandler
import ru.mazino.ponizzzer.services.SnmpContext

class V2405(snmp: SnmpContext) : BaseHandler(snmp) {
    override fun discover() {
        snmp.subtree(".1.3.6.1.4.1.34592.1.3.4.1.1.7.1", { e ->
            e?.variableBindings?.associate {
                val (root, port, position) = it.oid.value.takeLast(3)
                it.variable.toString() to ONU(root, port, position)
            }?.also { NewOnu.emit(snmp.host to it) }
            true
        })
    }
}