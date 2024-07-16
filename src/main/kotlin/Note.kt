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
    val channel: Int,
    val note: Int,
    val velocity: Int,
    val chance: Float,
    val startIndex: Int,
    val stopIndex: Int
) {
    fun isPlaying() = chance > Random.nextFloat()
    val noteName = NOTE_NAMES[note % NOTE_NAMES.size]
    val durationInTicks = stopIndex - startIndex
    fun noteOnMessage() = ShortMessage(ShortMessage.NOTE_ON, channel, note, velocity)
    fun noteOffMessage() = ShortMessage(ShortMessage.NOTE_OFF, channel, note, 0)

    override fun toString() = """
        note=$note
        channel=$channel
        velocity=$velocity
        chance=$chance
        """.trimIndent()
}