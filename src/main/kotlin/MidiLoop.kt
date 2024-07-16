package org.example

import Note
import javax.sound.midi.ShortMessage

const val TICKS_PER_BAR = 96

class MidiLoop(
    var amountTicks: Int = 96, val loop: Map<Int, Note> = mapOf()
) {
    var index: Int = amountTicks - 1
        private set
    var noteIndex: Int = loop.size - 1
    var currentNote: Note? = loop[0]
        private set

    fun tick(): ShortMessage? {
        index = (index + 1) % amountTicks
        if (loop.containsKey(index)) {
            noteIndex = (noteIndex + 1) % loop.size
            val currentNote = loop[index]!!
            if (currentNote.isPlaying()) {
                this.currentNote = currentNote
                return currentNote.noteOnMessage()
            }
            return null
        }
        val currentNote = currentNote
        if (currentNote != null && index == currentNote.stopIndex) {
            this.currentNote = null
            return currentNote.noteOffMessage()
        }
        return null
    }

    fun reset() {
        index = amountTicks - 1
        noteIndex = loop.size - 1
        currentNote = loop[0]
    }
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
    val loop = mutableMapOf<Int, Note>()
    for ((index, chance) in chances.withIndex()) {
        if (chance != 0.0f) {
            val noteStartIndex = (ticksPerNoteFloat * index).toInt()
            val nextNoteIndex = (ticksPerNoteFloat * (index + 1)).toInt()

            val noteLengthTicks = (nextNoteIndex - 1) - noteStartIndex
            val noteStopIndex = noteStartIndex + (noteLengthTicks * length).toInt()

            loop[noteStartIndex] = Note(0, note, 96, chance, noteStartIndex, noteStopIndex)
        }
    }
    return MidiLoop(amountTicks, loop.toMap())
}