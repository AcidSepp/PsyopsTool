package de.yw.psyops

import java.time.Duration

class WmaBpmCalculator(private val amountSamples: Int) : () -> Float {

    private val samples: MutableList<Long> = mutableListOf()

    @Synchronized
    fun next(currentMeasurement: Long) {
        while (samples.size >= amountSamples) {
            samples.removeLast()
        }
        samples.addFirst(currentMeasurement)
    }

    @Synchronized
    override fun invoke() = tickDurationToBpm(computeWma(samples))
}

private fun computeWma(list: List<Long>) = list.withIndex().map {
    it.value * (list.size - it.index)
}.sum() / triangleNumber(list.size)

private fun tickDurationToBpm(tickDurationNanos: Float): Float {
    return (1f / tickDurationNanos) * Duration.ofMinutes(1).toNanos() / 24
}

private fun triangleNumber(number: Int) = number * (number + 1) / 2f