package de.yw.psyops

import javax.sound.midi.ShortMessage
import kotlin.random.Random

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

@Suppress("MemberVisibilityCanBePrivate")
class Note(
    var channel: Int,
    var midiNote: Int,
    var velocity: Int,
    var chance: Float,
    val startIndex: Int,
    val stopIndex: Int
) {
    fun isPlaying() = chance > Random.nextFloat()
    val name get() = NOTE_NAMES[midiNote % NOTE_NAMES.size]
    val durationInTicks = stopIndex - startIndex
    fun noteOnMessage() = ShortMessage(ShortMessage.NOTE_ON, channel, midiNote, velocity)
    fun noteOffMessage() = ShortMessage(ShortMessage.NOTE_OFF, channel, midiNote, 0)

    override fun toString() = """
        note=$midiNote
        channel=$channel
        velocity=$velocity
        chance=$chance
        """.trimIndent()

    fun increaseChannel() {
        channel = (channel + 1).coerceIn(0, 15)
    }

    fun decreaseChannel() {
        channel = (channel - 1).coerceIn(0, 15)
    }

    fun increasePitch() {
        midiNote = (midiNote + 1).coerceIn(0, 127)
    }

    fun decreasePitch() {
        midiNote = (midiNote - 1).coerceIn(0, 127)
    }

    fun increaseVelocity() {
        velocity = (velocity + 1).coerceIn(0, 127)
    }

    fun decreaseVelocity() {
        velocity = (velocity - 1).coerceIn(0, 127)
    }

    fun increaseChance() {
        chance = (chance + 0.1f).coerceIn(0f, 1f)
    }

    fun decreaseChance() {
        chance = (chance - 0.1f).coerceIn(0f, 1f)
    }
}

fun Note.containsTick(tick: Int) = tick in startIndex..stopIndex