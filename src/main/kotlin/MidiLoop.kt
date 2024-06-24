package org.example

import kotlinx.serialization.Serializable
import javax.sound.midi.ShortMessage
import kotlin.random.Random

const val TICKS_PER_BAR = 96
val NOTE_NAMES = arrayOf(
    "C",
    "C#",
    "D",
    "D#",
    "E",
    "F",
    "F#",
    "G",
    "G#",
    "A",
    "A#",
    "B"
)

@Serializable
class MidiLoop(
    var amountTicks: Int = 96, val loop: Map<Int, Event> = mapOf()
) {
    var index: Int = amountTicks - 1
        private set
    var noteIndex: Int = loop.size - 1
    var currentNote: Event? = loop[0]
        private set

    fun tick(): Event? {
        index = (index + 1) % amountTicks
        if (loop.containsKey(index)) {
            noteIndex = (noteIndex + 1) % loop.size
            currentNote = loop[index]!!
            return currentNote
        }
        val currentNote = currentNote
        if (currentNote != null && index == currentNote.stopIndex) {
            this.currentNote = null
            return currentNote.asNoteOff()
        }
        return null
    }
}

@Serializable
class Event(
    val command: Int,
    val channel: Int,
    val note: Int,
    val velocity: Int,
    val chance: Float,
    val startIndex: Int,
    val stopIndex: Int
) {
    fun isPlaying() = chance > Random.nextFloat()
    fun asShortMessage() = ShortMessage(command, channel, note, velocity)
    val noteName = NOTE_NAMES[note % NOTE_NAMES.size]
    val durationInTicks = stopIndex - startIndex
    fun asNoteOff() = Event(ShortMessage.NOTE_OFF, channel, note, 96, chance, startIndex, stopIndex)
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
fun fillSteps(chances: FloatArray, subdivisions: Int, note: Int, length: Float = 1f): MidiLoop {
    val ticksPerNoteFloat = TICKS_PER_BAR.toFloat() / subdivisions
    val amountTicks = (chances.size * ticksPerNoteFloat).toInt()
    val loop = mutableMapOf<Int, Event>()
    for ((index, chance) in chances.withIndex()) {
        if (chance != 0.0f) {
            val noteStartIndex = (ticksPerNoteFloat * index).toInt()
            val nextNoteIndex = (ticksPerNoteFloat * (index + 1)).toInt()

            val noteLengthTicks = (nextNoteIndex - 1) - noteStartIndex
            val noteStopIndex = noteStartIndex + (noteLengthTicks * length).toInt()

            loop[noteStartIndex] = Event(ShortMessage.NOTE_ON, 0, note, 96, chance, noteStartIndex, noteStopIndex)
        }
    }
    return MidiLoop(amountTicks, loop.toMap())
}