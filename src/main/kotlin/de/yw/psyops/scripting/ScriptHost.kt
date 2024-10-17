package de.yw.psyops.scripting

import de.yw.psyops.MidiLoop
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

fun loadLoopsFromScript(): List<MidiLoop> {
    val scriptFile =
        File("/home/yannick/IdeaProjects/PsyopsTool/src/main/resources/dnb.kts")
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScript> {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }
    val pluginContextImpl = PluginContextImpl()
    val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<SimpleScript> {
        constructorArgs.put(listOf(pluginContextImpl))
    }
    val res =
        BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, evaluationConfiguration)
    res.reports.forEach {
        if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
            println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
        }
    }
    return pluginContextImpl.midiLoops
}

data class PluginContextImpl(override var midiLoops: MutableList<MidiLoop> = mutableListOf()) : PluginContext
