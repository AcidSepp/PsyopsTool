package org.example

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage


private const val ticksPerQuarterNote = 24
private const val millisecondsPerMinute = 60_000

fun main() {

    val loop1 = oneBarMidiLoop(booleanArrayOf(true, true, true, true, true, true, true, true), 41)
    val loop2 = oneBarMidiLoop(booleanArrayOf(true, false, false, false, false, true, false, false), 36)
    val loop3 = oneBarMidiLoop(booleanArrayOf(false, false, true, false, false, false, true, false), 38)

    val loops = listOf(loop1, loop2, loop3)

    println("ALL MIDI DEVICES")
    MidiSystem.getMidiDeviceInfo().forEach(::println)

    val midiOutDevices = MidiSystem.getMidiDeviceInfo()
        .map {
            MidiSystem.getMidiDevice(it)
        }.filter {
            it.maxReceivers != 0
        }.filter {
            it.deviceInfo.name.contains("DrumBrute")
        }.onEach {
            println("Name: ${it.deviceInfo.name} Desc: ${it.deviceInfo.description}")
            println("MaxReceivers: ${it.maxReceivers} MaxTransmitters: ${it.maxTransmitters}")
            println()
        }

    val device = midiOutDevices.first()
    device.open()

    val tickDuration = getTickDurationFromBpm(176.0f)
    println("Step duration $tickDuration ms")

    MidiLoopVisualiser(listOf(loop1, loop2, loop3))

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
                device.receiver.send(event, -1)
            }
        }
    }, 0, tickDuration, TimeUnit.MILLISECONDS)
}

private fun getTickDurationFromBpm(bpm: Float): Long {
    return ((1f / (bpm * ticksPerQuarterNote)) * millisecondsPerMinute).toLong()
}