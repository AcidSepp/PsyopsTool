import org.assertj.core.api.Assertions.assertThat
import org.example.oneBarMidiLoop
import org.junit.jupiter.api.Test
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON

class MidiLoopTest {

    @Test
    fun testQuarterNotes() {
        val loop = oneBarMidiLoop(booleanArrayOf(true, true, true, true), 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_ON.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
            repeat(22) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_OFF.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
        }
    }

    @Test
    fun testQuarterNotes2() {
        val loop = oneBarMidiLoop(booleanArrayOf(true, false, true, false), 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_ON.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
            repeat(22) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_OFF.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
            // no note should be played
            repeat(24) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun test8thNotes() {
        val loop = oneBarMidiLoop(booleanArrayOf(true, false, true, false, true, false, true, false), 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_ON.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
            repeat(10) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_OFF.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
            // no note should be played
            repeat(12) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun test7thNotes() {
        val loop = oneBarMidiLoop(booleanArrayOf(true, true, true, true, true, true, true), 36)

        // this note takes 13 steps
        assertThat(loop.tick()).matches {
            it!!
            it.shortMessage.message[0] == NOTE_ON.toByte() &&
                    it.shortMessage.message[1] == 36.toByte()
        }
        repeat(11) {
            assertThat(loop.tick()).isNull()
        }
        assertThat(loop.tick()).matches {
            it!!
            it.shortMessage.message[0] == NOTE_OFF.toByte() &&
                    it.shortMessage.message[1] == 36.toByte()
        }

        // this note takes 14 steps
        assertThat(loop.tick()).matches {
            it!!
            it.shortMessage.message[0] == NOTE_ON.toByte() &&
                    it.shortMessage.message[1] == 36.toByte()
        }
        repeat(12) {
            assertThat(loop.tick()).isNull()
        }
        assertThat(loop.tick()).matches {
            it!!
            it.shortMessage.message[0] == NOTE_OFF.toByte() &&
                    it.shortMessage.message[1] == 36.toByte()
        }
    }
}