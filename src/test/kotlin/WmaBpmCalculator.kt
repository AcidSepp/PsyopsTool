import de.yw.psyops.WmaBpmCalculator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.time.Duration

class WmaBpmCalculator {

    @Test
    fun `60bpm`() {
        val wmaBpmCalculator = WmaBpmCalculator(5)

        val tickDuration60bpmNanos = Duration.ofSeconds(1).toNanos() / 24

        wmaBpmCalculator.next(tickDuration60bpmNanos)
        assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
        wmaBpmCalculator.next(tickDuration60bpmNanos)
        assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
        wmaBpmCalculator.next(tickDuration60bpmNanos)
        assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
        wmaBpmCalculator.next(tickDuration60bpmNanos)
        assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
        wmaBpmCalculator.next(tickDuration60bpmNanos)
        assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
        wmaBpmCalculator.next(tickDuration60bpmNanos)
        assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
    }

    @Test
    fun changeFrom60bpmTo120bpm() {
        val wmaBpmCalculator = WmaBpmCalculator(5)
        val tickDuration60bpmNanos = Duration.ofSeconds(1).toNanos() / 24
        val tickDuration120bpmNanos = Duration.ofMillis(500).toNanos() / 24
        repeat(5) {
            wmaBpmCalculator.next(tickDuration60bpmNanos)
            assertThat(wmaBpmCalculator.invoke()).isCloseTo(60f, Offset.offset(0.1f))
        }
        repeat(5) {
            wmaBpmCalculator.next(tickDuration120bpmNanos)
        }
        repeat(5) {
            wmaBpmCalculator.next(tickDuration120bpmNanos)
            assertThat(wmaBpmCalculator.invoke()).isCloseTo(120f, Offset.offset(0.1f))
        }
    }
}