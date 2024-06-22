package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.float
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage

private const val ticksPerQuarterNote = 24
private const val millisecondsPerMinute = 60_000

fun main(args: Array<String>) = PsyopsTool().main(args)

class PsyopsTool : CliktCommand() {

    private val visualization: Boolean by option().boolean().default(true).help("Show visualization")
    private val device: String by option().default("Gervill").help("Show visualization")
    private val bpm: Float by option().float().default(176f).help("Beats per minute")

    override fun run() {
        val loops = listOf(
            fillOneBarMidiLoop(8, 41),
            fillOneBarMidiLoopWithChances(floatArrayOf(1.0f, 0.25f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.25f), 36),
            fillOneBarMidiLoopWithChances(floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.25f), 38),
        )
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
        println(json.encodeToString(loops))

        println("ALL MIDI DEVICES")
        MidiSystem.getMidiDeviceInfo().forEach(::println)

        val midiOutDevices = MidiSystem.getMidiDeviceInfo()
            .map {
                MidiSystem.getMidiDevice(it)
            }.filter {
                it.maxReceivers != 0
            }.filter {
                it.deviceInfo.name.contains(device)
            }.onEach {
                println("Name: ${it.deviceInfo.name} Desc: ${it.deviceInfo.description}")
                println("MaxReceivers: ${it.maxReceivers} MaxTransmitters: ${it.maxTransmitters}")
                println()
            }

        val device = midiOutDevices.first()
        device.open()

        val tickDuration = getTickDurationFromBpm(bpm)
        println("Step duration $tickDuration ms")

        Runtime.getRuntime().addShutdownHook(Thread {
            device.receiver.send(ShortMessage(ShortMessage.STOP), -1)
            device.receiver.send(ShortMessage(ShortMessage.SYSTEM_RESET), -1)
            device.receiver.send(ShortMessage(0xF3, 0, 0), -1) // All notes off
        })

        device.receiver.send(ShortMessage(ShortMessage.START), -1)

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            val clock = ShortMessage(ShortMessage.TIMING_CLOCK)
            device.receiver.send(clock, -1)

            loops.forEach {
                val event = it.tick()
                if (event != null) {
                    if (event.isPlaying()) {
                        device.receiver.send(event.asShortMessage(), -1)
                    }
                }
            }
        }, 0, tickDuration, TimeUnit.MILLISECONDS)

        if (visualization) {
            MidiLoopVisualiser(loops)
        }
    }

}

private fun getTickDurationFromBpm(bpm: Float): Long {
    return ((1f / (bpm * ticksPerQuarterNote)) * millisecondsPerMinute).toLong()
}