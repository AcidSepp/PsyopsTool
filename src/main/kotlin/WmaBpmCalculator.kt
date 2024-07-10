package org.example

import java.time.Duration

class WmaBpmCalculator(private val amountSamples: Int) {

    private val samples: MutableList<Long> = mutableListOf()

    fun next(currentMeasurement: Long): Float {
        while (samples.size >= amountSamples) {
            samples.removeLast()
        }
        samples.addFirst(currentMeasurement)
        return tickDurationToBpm(computeWma(samples))
    }
}

private fun computeWma(list: List<Long>) =
    list.withIndex().map {
        it.value * (list.size - it.index)
    }.sum() / triangleNumber(list.size)

private fun tickDurationToBpm(tickDurationNanos: Float): Float {
    return (1f / tickDurationNanos) * Duration.ofMinutes(1).toNanos() / 24
}

private fun triangleNumber(number: Int) = number * (number + 1) / 2f