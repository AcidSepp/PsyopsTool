package de.yw.psyops

import de.yw.psyops.Printer.State.*

class Printer(
    private val loops: List<MidiLoop>,
    private val bpmProvider: () -> Float,
    private val inputDeviceFullName: String?,
    private val outputDeviceFullName: String,
    private val clockMode: ClockMode
) {

    @Volatile
    private var display = NOTE

    private var midiLoopsAsStrings = listOf<String>()


    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            makeCursorVisible()
        })
        makeCursorInvisible()
        reset()
    }

    fun displayChances() {
        display = CHANCE
        reset()
    }

    fun displayNotes() {
        display = NOTE
        reset()
    }

    fun displayVelocities() {
        display = VELOCITY
        reset()
    }

    fun reset() {
        erase()
        printTopLine()
        midiLoopsAsStrings = loops.map { midiLoop ->
            var result = ""
            var skip = 0
            for (tickIndex in 0 until midiLoop.amountTicks) {
                if (skip != 0) {
                    skip--
                    continue
                }
                val note = midiLoop.noteMap[tickIndex]
                if (note == null) {
                    result += "."
                } else {
                    when (display) {
                        NOTE -> {
                            skip = note.name.length - 1
                            result += note.name
                        }

                        CHANCE -> {
                            val chanceString = "%.1f".format(note.chance)
                            skip = chanceString.length - 1
                            result += chanceString
                        }

                        VELOCITY -> {
                            val velocityString = note.velocity.toString()
                            skip = velocityString.length - 1
                            result += velocityString
                        }
                    }
                }
            }
            return@map result
        }
        midiLoopsAsStrings.forEach(::println)
    }

    fun update() {
        printTopLine()
        for ((midiLoopIndex, midiLoop) in loops.withIndex()) {
            val midiLoopAsString = midiLoopsAsStrings[midiLoopIndex]

            resetUnderline()
            printAt(midiLoopIndex + 1, midiLoop.previousTick, midiLoopAsString[midiLoop.previousTick].toString())

            setUnderline()
            printAt(midiLoopIndex + 1, midiLoop.currentTick, midiLoopAsString[midiLoop.currentTick].toString())
            resetUnderline()
        }
    }

    private fun printTopLine() {
        val inputDevice = " inputDevice=${inputDeviceFullName}"
        printAt(
            0,
            0,
            "bpm=${"%.0f".format(bpmProvider())} outputDevice=${outputDeviceFullName}$inputDevice clockMode=${clockMode}"
        )
        eraseUntilEndOfLine()
        println()
    }

    private enum class State {
        NOTE,
        CHANCE,
        VELOCITY
    }
}
