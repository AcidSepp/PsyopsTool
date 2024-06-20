package org.example

import javax.sound.midi.ShortMessage
import kotlin.random.Random

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

fun fillOneBarMidiLoop(subdivisions: Int, note: Int, ticksPerBar: Int = 96): MidiLoop {
    val array = BooleanArray(subdivisions)
    array.fill(true)
    return oneBarMidiLoop(array, ticksPerBar, note)
}

fun oneBarMidiLoop(events: BooleanArray, note: Int, ticksPerBar: Int = 96): MidiLoop {
    val ticksPerNoteFloat = ticksPerBar.toFloat() / events.size

    val loop = mutableMapOf<Int, Event>()

    for ((index, event) in events.withIndex()) {
        if (event) {
            val noteStartIndex = (ticksPerNoteFloat * index).toInt()
            val nextNoteIndex = (ticksPerNoteFloat * (index + 1)).toInt()
            val noteStopIndex = nextNoteIndex - 1

            loop[noteStartIndex] = Event(ShortMessage(ShortMessage.NOTE_ON, 0, note, 96), 1.0f)
            loop[noteStopIndex] = Event(ShortMessage(ShortMessage.NOTE_OFF, 0, note, 96), 1.0f)
        }
    }

    return MidiLoop(ticksPerBar, loop.toMap())
}

fun oneBarMidiLoopWithChances(chances: FloatArray, note: Int, ticksPerBar: Int = 96): MidiLoop {
    val ticksPerNoteFloat = ticksPerBar.toFloat() / chances.size

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

    return MidiLoop(ticksPerBar, loop.toMap())
}

class Event(val shortMessage: ShortMessage, val chance: Float) {

    fun isPlaying() = chance > Random.nextFloat()

}