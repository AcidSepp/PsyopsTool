package de.yw.psyops

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = "simplescript.kts"
)
// the class is used as the script base class, therefore it should be open or abstract
abstract class SimpleScript(val pluginContext: PluginContext) {
    var midiLoops: MutableList<MidiLoop> by pluginContext::midiLoops
}

interface PluginContext {
    var midiLoops: MutableList<MidiLoop>
}