package de.yw.psyops

import javax.sound.midi.ShortMessage

const val TICKS_PER_BAR = 96

class MidiLoop(
    var amountTicks: Int = 96, val noteMap: Map<Int, Note> = mapOf()
) {
    var notesList = noteMap.entries.sortedBy { entry -> entry.key }.map { entry -> entry.value }

    var currentTick = lastTick
        private set
    var noteIndex: Int = lastNoteIndex
    var currentNote: Note? = noteMap[0]
        private set

    fun tick(): ShortMessage? {
        currentTick = nextTick
        if (noteMap.containsKey(currentTick)) {
            noteIndex = nextNoteIndex
            val currentNote = noteMap[currentTick]!!
            if (currentNote.isPlaying()) {
                this.currentNote = currentNote
                return currentNote.noteOnMessage()
            }
            return null
        }
        val currentNote = currentNote
        if (currentNote != null && currentTick == currentNote.stopIndex) {
            this.currentNote = null
            return currentNote.noteOffMessage()
        }
        return null
    }

    fun reset() {
        currentTick = amountTicks - 1
        noteIndex = noteMap.size - 1
        currentNote = noteMap[0]
    }
}

/**
 * Fills one bar with the amount of notes given.
 * @param noteCount the count of notes in this bar. Can be an odd number.
 */
fun fillOneBarMidiLoop(
    noteCount: Int, note: Int, length: Float = 1f, channel: Int = 0, velocity: Int = 127, probability: Float = 1f
): MidiLoop {
    check(noteCount > 0)
    val probabilitys = FloatArray(noteCount)
    probabilitys.fill(probability)
    return fillOneBarMidiLoopWithProbabilities(probabilitys, note, length, channel, velocity)
}

/**
 * Fills one bar with notes with the given probabilitys.
 * The size of the array determines the subdivision of the notes.
 */
fun fillOneBarMidiLoopWithProbabilities(
    probabilitys: FloatArray, note: Int, length: Float = 1f, channel: Int = 0, velocity: Int = 127
): MidiLoop {
    return fillSteps(probabilitys, probabilitys.size, note, length, channel, velocity)
}

/**
 * Creates a Midi Loop with notes with the given probabilitys.
 * This can be longer or shorter than a bar.
 */
fun fillSteps(
    probabilitys: FloatArray, subdivisions: Int, note: Int, length: Float = 1f, channel: Int = 0, velocity: Int = 127
): MidiLoop {
    val ticksPerNoteFloat = TICKS_PER_BAR.toFloat() / subdivisions
    val amountTicks = (probabilitys.size * ticksPerNoteFloat).toInt()
    val loop = mutableMapOf<Int, Note>()
    for ((index, probability) in probabilitys.withIndex()) {
        val noteStartIndex = (ticksPerNoteFloat * index).toInt()
        val nextNoteIndex = (ticksPerNoteFloat * (index + 1)).toInt()

        val noteLengthTicks = (nextNoteIndex - 1) - noteStartIndex
        val noteStopIndex = noteStartIndex + (noteLengthTicks * length).toInt()

        loop[noteStartIndex] = Note(channel, note, velocity, probability, noteStartIndex, noteStopIndex)
    }
    return MidiLoop(amountTicks, loop.toMap())
}

val MidiLoop.previousTick
    get() = if (currentTick == 0) {
        amountTicks - 1
    } else {
        (currentTick - 1) % amountTicks
    }

val MidiLoop.nextTick
    get() = (currentTick + 1) % amountTicks

val MidiLoop.lastTick
    get() = amountTicks - 1

private val MidiLoop.lastNoteIndex
    get() = noteMap.size - 1

private val MidiLoop.nextNoteIndex
    get() = (noteIndex + 1) % noteMap.size
