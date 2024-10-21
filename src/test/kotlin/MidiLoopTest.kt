import de.yw.psyops.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON

class MidiLoopTest {

    @Test
    fun fillOneBarMidiLoop_halfNotes() {
        val loop = fillOneBarMidiLoop(2, 36)

        repeat(2) {
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(46) {
                assertThat(loop.tick()).isNull()
                assertThat(loop.noteIndex == 0)
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(46) {
                assertThat(loop.tick()).isNull()
                assertThat(loop.noteIndex == 1)
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
        }
    }

    @Test
    fun fillOneBarMidiLoopWithProbablities_quarterNotes() {
        val loop = fillOneBarMidiLoopWithProbabilities(floatArrayOf(1f, 1f, 1f, 1f), 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(22) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
        }
    }

    @Test
    fun fillOneBarMidiLoopWithProbablities_quarterNotes2() {
        val loop = fillOneBarMidiLoopWithProbabilities(floatArrayOf(1f, 0f, 1f, 0f), 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(22) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
            // no note should be played
            repeat(24) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun fillOneBarMidiLoopWithProbablities_8thNotes() {
        val loop = fillOneBarMidiLoopWithProbabilities(floatArrayOf(1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f), 36)

        repeat(4) {
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(10) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
            // no note should be played
            repeat(12) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun fillOneBarMidiLoopWithProbablities_7thNotes() {
        val loop = fillOneBarMidiLoopWithProbabilities(floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f), 36)

        // this note takes 13 steps
        assertThat(loop.tick()).matches {
            it!!
            it.command == NOTE_ON &&
                    it.data1 == 36
        }
        repeat(11) {
            assertThat(loop.tick()).isNull()
        }
        assertThat(loop.tick()).matches {
            it!!
            it.command == NOTE_OFF &&
                    it.data1 == 36
        }

        // this note takes 14 steps
        assertThat(loop.tick()).matches {
            it!!
            it.command == NOTE_ON &&
                    it.data1 == 36
        }
        repeat(12) {
            assertThat(loop.tick()).isNull()
        }
        assertThat(loop.tick()).matches {
            it!!
            it.command == NOTE_OFF &&
                    it.data1 == 36
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
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(22) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
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
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(22) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
            // wait for 4 quarter notes
            repeat(96) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun fillSteps_4quarterNotes_halfLength() {
        val loop = fillSteps(floatArrayOf(1f, 0f, 0f, 0f), 4, 36, 0.5f)

        assertThat(loop.amountTicks).isEqualTo(96)

        repeat(2) {
            // first note is on
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_ON &&
                        it.data1 == 36
            }
            repeat(10) {
                assertThat(loop.tick()).isNull()
            }
            assertThat(loop.tick()).matches {
                it!!
                it.command == NOTE_OFF &&
                        it.data1 == 36
            }
            // wait for 4 quarter notes
            repeat(84) {
                assertThat(loop.tick()).isNull()
            }
        }
    }

    @Test
    fun indexProperties() {
        val loop = fillSteps(floatArrayOf(1f, 0.3f, 0f, 0.5f), 4, 36, 0.5f)
        assertThat(loop.amountTicks).isEqualTo(96)
        loop.tick()
        assertThat(loop.currentTick).isEqualTo(0)
        assertThat(loop.nextTick).isEqualTo(1)
        assertThat(loop.previousTick).isEqualTo(95)

        assertThat(loop.currentNote!!.probability).isEqualTo(1f)
    }
}