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

class Event(val shortMessage: ShortMessage, val chance: Float) {
    fun isPlaying() = chance > Random.nextFloat()
}

/**
 * Fills one bar with the amount of notes given.
 * @param noteCount the count of notes in this bar. Can be an odd number.
 */
fun fillOneBarMidiLoop(noteCount: Int, note: Int): MidiLoop {
    check(noteCount > 0)
    val array = FloatArray(noteCount)
    array.fill(1.0f)
    return fillOneBarMidiLoopWithChances(array, note)
}

/**
 * Fills one with either pauses or the note, depending on the array.
 * The size of the array determines the subdivision of the notes.
 */
fun fillOneBarMidiLoop(events: BooleanArray, note: Int): MidiLoop {
    val chances = events.map { if (it) 1.0f else 0.0f }.toFloatArray()
    return fillOneBarMidiLoopWithChances(chances, note)
}

/**
 * Fills one bar with notes with the given chances.
 * The size of the array determines the subdivision of the notes.
 */
fun fillOneBarMidiLoopWithChances(chances: FloatArray, note: Int): MidiLoop {
    return fillSteps(chances, chances.size, note)
}

/**
 * Creates a Midi Loop with notes with the given chances.
 * This can be longer or shorter than a bar.
 */
fun fillSteps(chances: FloatArray, subdivisions: Int, note: Int): MidiLoop {
    val ticksPerNoteFloat = TICKS_PER_BAR.toFloat() / subdivisions
    val amountTicks = (chances.size * ticksPerNoteFloat).toInt()
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
    return MidiLoop(amountTicks, loop.toMap())
}