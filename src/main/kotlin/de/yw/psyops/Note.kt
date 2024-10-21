package de.yw.psyops

import de.yw.psyops.masks.GenericKeyboardMask
import de.yw.psyops.masks.NoteNameMask
import javax.sound.midi.ShortMessage
import kotlin.random.Random

@Suppress("MemberVisibilityCanBePrivate")
class Note(
    var channel: Int,
    var midiPitch: Int,
    var velocity: Int,
    var percentage: Float,
    val startIndex: Int,
    val stopIndex: Int,
) {
    fun isPlaying() = percentage > Random.nextFloat()
    val durationInTicks = stopIndex - startIndex
    fun noteOnMessage() = ShortMessage(ShortMessage.NOTE_ON, channel, midiPitch, velocity)
    fun noteOffMessage() = ShortMessage(ShortMessage.NOTE_OFF, channel, midiPitch, 0)

    override fun toString() = """
        note=$midiPitch
        channel=$channel
        velocity=$velocity
        percentage=$percentage
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
        percentage = (percentage + 0.1f).coerceIn(0f, 1f)
    }

    fun decreaseChance() {
        percentage = (percentage - 0.1f).coerceIn(0f, 1f)
    }
}

fun Note.containsTick(tick: Int) = tick in startIndex..stopIndex