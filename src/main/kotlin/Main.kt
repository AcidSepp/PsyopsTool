package org.example

import javax.sound.midi.MidiSystem

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

    val matriarch = midiOutDevices.first()
    matriarch.open()

    val tickDuration = getTickDurationFromBpm(100.0f)
    println("Step duration $tickDuration ms")

    val midiLoopVisualiser1 = MidiLoopVisualiser(loop1)
    val midiLoopVisualiser2 = MidiLoopVisualiser(loop2)

    repeat(100000) {
        Thread.sleep(tickDuration)

        val loop1Event = loop1.tick()
        if (loop1Event != null) {
            matriarch.receiver.send(loop1Event, -1)
        }

        val loop2Event = loop2.tick()
        if (loop2Event != null) {
            matriarch.receiver.send(loop2Event, -1)
        }

        midiLoopVisualiser1.repaint()
        midiLoopVisualiser2.repaint()
    }
}

private fun getTickDurationFromBpm(bpm: Float): Long {
    return ((1f / (bpm * ticksPerQuarterNote)) * millisecondsPerMinute).toLong()
}