package de.yw.psyops.scripting

import de.yw.psyops.MidiLoop
import java.io.File
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.util.isError
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

fun loadLoopsFromScript(
    scriptFile: File
): List<MidiLoop> {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScript> {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
            defaultImports.put(listOf("de.yw.psyops.*", "de.yw.psyops.mappings.*"))
        }
    }
    val pluginContextImpl = PluginContextImpl()
    val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<SimpleScript> {
        constructorArgs.put(listOf(pluginContextImpl))
    }
    val scriptRunResult =
        BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, evaluationConfiguration)

    if (scriptRunResult.isError()) {
        scriptRunResult.reports.forEach {
            println(it.message + if (it.exception == null) "" else ": ${it.exception}")
        }
        throw RuntimeException("Could not execute script ${scriptFile.absolutePath}")
    }

    return pluginContextImpl.midiLoops
}

data class PluginContextImpl(override var midiLoops: MutableList<MidiLoop> = mutableListOf()) : PluginContext
