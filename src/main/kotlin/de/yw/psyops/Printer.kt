package de.yw.psyops

class Printer(
    private val loops: List<MidiLoop>,
    private val bpmProvider: () -> Float,
    private val inputDeviceFullName: String?,
    private val outputDeviceFullName: String,
    private val clockMode: ClockMode
) {

    private val midiLoopsAsStrings: List<String>

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            makeCursorVisible()
        })
        makeCursorInvisible()
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
                    skip = note.name.length - 1
                    result += note.name
                }
            }
            return@map result
        }
    }

    fun reset() {
        erase()
        printTopLine()
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
}
