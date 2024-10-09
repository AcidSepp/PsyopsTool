class Printer(private val loops: List<MidiLoop>, private val bpmProvider: () -> Float) {

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            makeCursorVisible()
        })
        moveCursor(0, 0)
        erase()
        makeCursorInvisible()
        for ((loopIndex, midiLoop) in loops.withIndex()) {
            var skip = 0
            for (tickIndex in 0 until midiLoop.amountTicks) {
                if (skip != 0) {
                    skip--
                    continue
                }
                val note = midiLoop.noteMap[tickIndex]
                if (note == null) {
                    printAt(loopIndex, tickIndex, ".")
                } else {
                    skip = note.name.length - 1
                    printAt(loopIndex, tickIndex, note.name)
                }
            }
        }
    }

    fun update() {
        moveCursor(0, 0)
        for ((loopIndex, midiLoop) in loops.withIndex()) {
            val lastTick = midiLoop.previousTick
            resetUnderline()
            val noteAtLastTick = midiLoop.noteMap[lastTick]
            if (noteAtLastTick == null) {
                printAt(loopIndex, lastTick,".")
            } else {
                printAt(loopIndex, lastTick, noteAtLastTick.name)
            }

            setUnderline()
            val currentNote = midiLoop.currentNote
            if (currentNote != null) {
                if (midiLoop.currentTick - currentNote.startIndex == 0) {
                    printAt(loopIndex, midiLoop.currentTick, currentNote.name)
                } else if (midiLoop.currentTick - currentNote.startIndex < currentNote.name.length) {
                    continue
                } else {
                    printAt(loopIndex, midiLoop.currentTick,".")
                }
            } else {
                printAt(loopIndex, midiLoop.currentTick,".")

            }
            resetUnderline()
        }
    }
}
