package ru.mazino.ponizzzer

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import ru.mazino.ponizzzer.models.DeviceHandler
import ru.mazino.ponizzzer.services.SnmpContext
import java.io.File
import javax.script.ScriptEngineManager
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

private val engine = ScriptEngineManager().getEngineByExtension("kts")
private val logger = LoggerFactory.getLogger("ScriptLoader")

fun loadScripts() = runBlocking {
    val pathToHandlers = "handlers"
    val metaPropertiesList = listOf("vendor", "model", "softwareVersion")
    val candidates = HashMap<Qualifier, KClass<DeviceHandler>>()
    println(engine.factory)
    File(pathToHandlers).walk().filter { it.isFile && it.extension == "kts" }
        .map {
            logger.info("Reading file ${it.name}")
            Regex("handlers/(.+)/(.+)/(.+)\\.kts", RegexOption.IGNORE_CASE)
                .find(it.invariantSeparatorsPath) to it.readText()
        }
        .forEach { entry ->
            runCatching {
                Regex("(?<=class\\s)([\\w\\s`]+)")
                    .find(entry.second)!!
                    .also { logger.info("Attempting compile and load ${it.value} class...") }
                    .run { load<KClass<DeviceHandler>>(entry.second + "\r\n$value::class") }
            }.getOrElse { ex -> println(ex); return@forEach }
                .apply {
                    logger.info("Class ${this.simpleName} successfully loaded.")
                    runCatching {
                        if (companionObject is KClass) {
                            metaPropertiesList.map {
                                companionObject?.memberProperties
                                    ?.find { kProp -> kProp.name == it }
                                    ?.call(companionObjectInstance) as String
                            }
                        } else entry.first?.destructured!!.toList()
                    }.getOrElse { throw RuntimeException("Cannot infer any class qualifier while loading from ${this.simpleName} class") }
                        .run { candidates[named(joinToString("/"))] = this@apply }
                }
        }

    loadKoinModules(module {
        candidates.forEach { (qualifier, clazz) ->
            factory(qualifier) {
                clazz.primaryConstructor?.call(get<SnmpContext>() { it })
            }
        }
    })
}


private inline fun <reified T> Any?.tryCast(): T = takeIf { it is T }?.let { it as T }
    ?: throw IllegalArgumentException("Impossible create type ${T::class} from $this")

private inline fun <reified T> load(content: String): T = runCatching { engine.eval(content) }
    .getOrElse { throw RuntimeException("Cannot load handler", it) }
    .tryCast()
