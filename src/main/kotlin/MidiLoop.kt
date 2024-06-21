package org.example

import javax.sound.midi.ShortMessage
import kotlin.random.Random

const val TICKS_PER_BAR = 96

class MidiLoop(
    var amountTicks: Int = 96, val loop: Map<Int, Event> = mapOf()
) {
    var index: Int = amountTicks - 1
        private set

    fun tick(): Event? {
        index = (index + 1) % amountTicks
        return if (loop.containsKey(index)) loop[index] else null
    }
}

fun fillOneBarMidiLoop(subdivisions: Int, note: Int): MidiLoop {
    val array = FloatArray(subdivisions)
    array.fill(1.0f)
    return fillOneBarMidiLoopWithChances(array, note)
}

fun fillOneBarMidiLoop(events: BooleanArray, note: Int): MidiLoop {
    val chances = events.map { if (it) 1.0f else 0.0f }.toFloatArray()
    return fillOneBarMidiLoopWithChances(chances, note)
}

fun fillOneBarMidiLoopWithChances(chances: FloatArray, note: Int): MidiLoop {
    val ticksPerNoteFloat = TICKS_PER_BAR.toFloat() / chances.size

    val loop = mutableMapOf<Int, Event>()

    for ((index, chance) in chances.withIndex()) {
        if (chance != 0.0f) {
            val noteStartIndex = (ticksPerNoteFloat * index).toInt()
            val nextNoteIndex = (ticksPerNoteFloat * (index + 1)).toInt()
            val noteStopIndex = nextNoteIndex - 1

            loop[noteStartIndex] = Event(ShortMessage(ShortMessage.NOTE_ON, 0, note, 96), chance)
            loop[noteStopIndex] = Event(ShortMessage(ShortMessage.NOTE_OFF, 0, note, 96), 1.0f)
        }
    }

    return MidiLoop(TICKS_PER_BAR, loop.toMap())
}

class Event(val shortMessage: ShortMessage, val chance: Float) {

    fun isPlaying() = chance > Random.nextFloat()

}