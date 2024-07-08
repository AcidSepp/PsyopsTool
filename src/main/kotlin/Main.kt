package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.float
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sound.midi.*

private const val ticksPerQuarterNote = 24

fun main(args: Array<String>) = PsyopsTool().main(args)

class PsyopsTool : CliktCommand() {

    private val visualization: Boolean by option().boolean().default(true).help("Show visualization.")
    private val outputDeviceName: String by option().default("Gervill").help("Output Device.")
    private val inputDeviceName: String by option().default("Gervill")
        .help("Input Device. Ignored in internal clock mode.")
    private val bpm: Float by option().float().default(80f).help("Beats per minute. Ignored in external clock mode.")
    private val clockMode: ClockMode by option().enum<ClockMode>().default(ClockMode.INTERNAL).help("Clock mode.")

    override fun run() {
        val loops = listOf(
            fillSteps(floatArrayOf(.6f, .6f, .6f, .6f, .6f, .6f, .6f, .6f), 16,43),
            fillSteps(floatArrayOf(.3f, .3f, .3f, .3f, .3f, .3f, .3f, .3f), 16,44),
            fillSteps(floatArrayOf(.5f), 1,45),
            fillSteps(floatArrayOf(1.0f, 0.25f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.25f), 16,36),
            fillSteps(floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.25f), 16, 37),
        )

        println("ALL MIDI DEVICES")
        MidiSystem.getMidiDeviceInfo().forEach(::println)

        val outputDevice = getOutputDevice(outputDeviceName)

        when (clockMode) {
            ClockMode.INTERNAL -> {
                internalClockMode(outputDevice, loops)
            }

            ClockMode.EXTERNAL -> {
                externalClockMode(inputDeviceName, outputDevice, loops)
            }
        }

        if (visualization) {
            MidiLoopVisualiser(loops)
        }
    }

    private fun internalClockMode(
        outputDevice: MidiDevice,
        loops: List<MidiLoop>
    ) {
        val tickDuration = getTickDurationFromBpm(bpm)
        println("Step duration $tickDuration ms")
        Runtime.getRuntime().addShutdownHook(Thread {
            outputDevice.receiver.send(ShortMessage(ShortMessage.STOP), -1)
            outputDevice.receiver.send(ShortMessage(ShortMessage.SYSTEM_RESET), -1)
            outputDevice.receiver.send(ShortMessage(0xF3, 0, 0), -1) // All notes off
        })
        outputDevice.receiver.send(ShortMessage(ShortMessage.START), -1)
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                val clock = ShortMessage(ShortMessage.TIMING_CLOCK)
                outputDevice.receiver.send(clock, -1)

                loops.forEach {
                    val event = it.tick()
                    if (event != null) {
                        if (event.isPlaying()) {
                            outputDevice.receiver.send(event.asShortMessage(), -1)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, tickDuration, TimeUnit.MILLISECONDS)
    }

    private fun externalClockMode(inputDeviceName: String, outputDevice: MidiDevice, loops: List<MidiLoop>) {
        val inputDevice = getInputDevice(inputDeviceName)
        Runtime.getRuntime().addShutdownHook(Thread {
            outputDevice.receiver.send(ShortMessage(ShortMessage.SYSTEM_RESET), -1)
            outputDevice.receiver.send(ShortMessage(0xF3, 0, 0), -1) // All notes off
        })
        inputDevice.transmitter.receiver = object : Receiver {
            override fun close() {
                TODO("Not yet implemented")
            }

            override fun send(message: MidiMessage, timeStamp: Long) {
                if (message.message[0] == ShortMessage.START.toByte() || message.message[0] == ShortMessage.STOP.toByte()) {
                    loops.forEach(MidiLoop::reset)
                }
                if (message.message[0] == ShortMessage.TIMING_CLOCK.toByte()) {
                    loops.forEach {
                        val event = it.tick()
                        if (event != null) {
                            if (event.isPlaying()) {
                                outputDevice.receiver.send(event.asShortMessage(), -1)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ClockMode {
    INTERNAL, EXTERNAL
}

private fun getTickDurationFromBpm(bpm: Float): Long {
    return ((1f / (bpm * ticksPerQuarterNote)) * Duration.ofMinutes(1).toMillis()).toLong()
}

private fun getOutputDevice(outputDeviceName: String): MidiDevice {
    val outputDevice = MidiSystem.getMidiDeviceInfo()
        .map {
            MidiSystem.getMidiDevice(it)
        }.filter {
            it.maxReceivers != 0
        }.filter {
            it.deviceInfo.name.contains(outputDeviceName)
        }.onEach {
            println("Name: ${it.deviceInfo.name} Desc: ${it.deviceInfo.description}")
            println("MaxReceivers: ${it.maxReceivers} MaxTransmitters: ${it.maxTransmitters}")
            println()
        }.first()!!
    outputDevice.open()
    return outputDevice
}

private fun getInputDevice(inputDeviceName: String): MidiDevice {
    val inputDevice = MidiSystem.getMidiDeviceInfo()
        .map {
            MidiSystem.getMidiDevice(it)
        }.filter {
            it.maxTransmitters != 0
        }.filter {
            it.deviceInfo.name.contains(inputDeviceName)
        }.onEach {
            println("Name: ${it.deviceInfo.name} Desc: ${it.deviceInfo.description}")
            println("MaxReceivers: ${it.maxReceivers} MaxTransmitters: ${it.maxTransmitters}")
            println()
        }.first()!!
    inputDevice.open()
    return inputDevice
}