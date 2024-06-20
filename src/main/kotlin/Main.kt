package org.example

import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage


private const val ticksPerQuarterNote = 24
private const val millisecondsPerMinute = 60_000

fun main() {

    val loop1 = oneBarMidiLoop(booleanArrayOf(true, true, true, true, true, true, true), 45)
    val loop2 = oneBarMidiLoop(booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true), 42)

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

    val tickDuration = getTickDurationFromBpm(100.0f)
    println("Step duration $tickDuration ms")

    val midiLoopVisualiser1 = MidiLoopVisualiser(loop1)
    val midiLoopVisualiser2 = MidiLoopVisualiser(loop2)

    device.receiver.send(ShortMessage(ShortMessage.START), -1)

    repeat(100000) {
        val clock = ShortMessage(ShortMessage.TIMING_CLOCK)
        device.receiver.send(clock, -1)

        val loop1Event = loop1.tick()
        if (loop1Event != null) {
            device.receiver.send(loop1Event, -1)
        }

        val loop2Event = loop2.tick()
        if (loop2Event != null) {
            device.receiver.send(loop2Event, -1)
        }

        midiLoopVisualiser1.repaint()
        midiLoopVisualiser2.repaint()

        Thread.sleep(tickDuration)
    }
}

private fun getTickDurationFromBpm(bpm: Float): Long {
    return ((1f / (bpm * ticksPerQuarterNote)) * millisecondsPerMinute).toLong()
}