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
    var midiPitch: Int,
    var velocity: Int,
    var chance: Float,
    val startIndex: Int,
    val stopIndex: Int
) {
    fun isPlaying() = chance > Random.nextFloat()
    val name get() = NOTE_NAMES[midiPitch % NOTE_NAMES.size]
    val durationInTicks = stopIndex - startIndex
    fun noteOnMessage() = ShortMessage(ShortMessage.NOTE_ON, channel, midiPitch, velocity)
    fun noteOffMessage() = ShortMessage(ShortMessage.NOTE_OFF, channel, midiPitch, 0)

    override fun toString() = """
        note=$midiPitch
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
        midiPitch = (midiPitch + 1).coerceIn(0, 127)
    }

    fun decreasePitch() {
        midiPitch = (midiPitch - 1).coerceIn(0, 127)
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