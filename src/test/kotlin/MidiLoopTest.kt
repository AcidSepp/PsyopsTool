import org.assertj.core.api.Assertions.assertThat
import org.example.fillOneBarMidiLoop
import org.example.fillSteps
import org.junit.jupiter.api.Test
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON

class MidiLoopTest {

    @Test
    fun fillOneBarMidiLoop_halfNotes() {
        val loop = fillOneBarMidiLoop(2, 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.shortMessage.message[0] == NOTE_ON.toByte() &&
                        it.shortMessage.message[1] == 36.toByte()
            }
            repeat(46) {
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
    fun fillOneBarMidiLoop_quarterNotes() {
        val loop = fillOneBarMidiLoop(booleanArrayOf(true, true, true, true), 36)

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
    fun fillOneBarMidiLoop_quarterNotes2() {
        val loop = fillOneBarMidiLoop(booleanArrayOf(true, false, true, false), 36)

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
    fun fillOneBarMidiLoop_8thNotes() {
        val loop = fillOneBarMidiLoop(booleanArrayOf(true, false, true, false, true, false, true, false), 36)

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
    fun fillOneBarMidiLoop_7thNotes() {
        val loop = fillOneBarMidiLoop(booleanArrayOf(true, true, true, true, true, true, true), 36)

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

    @Test
    fun fillSteps_3quarterNotes() {
        val loop = fillSteps(floatArrayOf(1f, 0f, 0f), 4, 36)

        assertThat(loop.amountTicks).isEqualTo(72)

        repeat(2) {
            // first note is on
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
            // wait for 2 quarter notes
            repeat(48) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun fillSteps_5quarterNotes() {
        val loop = fillSteps(floatArrayOf(1f, 0f, 0f, 0f, 0f), 4, 36)

        assertThat(loop.amountTicks).isEqualTo(120)

        repeat(2) {
            // first note is on
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
            // wait for 4 quarter notes
            repeat(96) {
                assertThat(loop.tick()).isNull()
            }
        }
    }
}