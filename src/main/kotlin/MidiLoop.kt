package org.example

import javax.sound.midi.ShortMessage

class MidiLoop(
    var amountTicks: Int = 96, val loop: Map<Int, ShortMessage> = mapOf()
) {
    var index: Int = amountTicks - 1
        private set

    fun tick(): ShortMessage? {
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

    val loop = mutableMapOf<Int, ShortMessage>()

    for ((index, event) in events.withIndex()) {
        if (event) {
            val noteStartIndex = (ticksPerNoteFloat * index).toInt()
            val nextNoteIndex = (ticksPerNoteFloat * (index + 1)).toInt()
            val noteStopIndex = nextNoteIndex - 1

            loop[noteStartIndex] = ShortMessage().apply {
                setMessage(ShortMessage.NOTE_ON, 0, note, 96)
            }
            loop[noteStopIndex] = ShortMessage().apply {
                setMessage(ShortMessage.NOTE_OFF, 0, note, 96)
            }
        }
    }

    return MidiLoop(ticksPerBar, loop.toMap())
}