package de.yw.psyops.ui

import de.yw.psyops.*
import de.yw.psyops.masks.NoteNameMask
import de.yw.psyops.ui.Printer.State.*
import org.jline.terminal.Terminal
import java.util.*

class Printer(
    private val loops: List<MidiLoop>,
    private val bpmProvider: () -> Float,
    private val inputDeviceFullName: String?,
    private val outputDeviceFullName: String,
    private val clockMode: ClockMode,
    private val terminal: Terminal,
    private val noteNameMask: NoteNameMask
) {

    @Volatile
    private var displayMode = NOTE_NAME
    private var midiLoopsAsStrings = listOf<String>()

    private var lastWidth = 0
    private var lastHeight = 0

    @Volatile
    var selectedNoteIndex = 0

    @Volatile
    var selectedLoopIndex = 0

    @Volatile
    var selectedNote: Note = loops[0].notesList[0]

    @Volatile
    var selectedLoop: MidiLoop = loops[0]

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            makeCursorVisible()
            enableLineWrapping()
        })
        reset()
    }

    fun reset() {
        Locale.setDefault(Locale.US)
        erase()
        disableLineWrapping()
        makeCursorInvisible()
        printTopLine()
        printBottomLine()
        midiLoopsAsStrings = loops.map { midiLoop ->
            var result = ""
            var skip = 0
            var lastPrintedNote: Note? = null
            for (tickIndex in 0 until midiLoop.amountTicks) {
                if (skip != 0) {
                    skip--
                    continue
                }
                val note = midiLoop.noteMap[tickIndex]
                if (note == null) {
                    result += if (lastPrintedNote != null && lastPrintedNote.containsTick(tickIndex)) {
                        "•"
                    } else {
                        "·"
                    }
                    continue
                }

                if (note.percentage == 0f) {
                    result += "_"
                    continue
                }

                lastPrintedNote = note
                when (displayMode) {
                    NOTE_NAME -> {
                        skip = noteNameMask[note.midiPitch].length - 1
                        result += noteNameMask[note.midiPitch]
                    }

                    PERCENTAGE -> {
                        val percentageString = "%.0f".format(note.percentage * 100) + "%"
                        skip = percentageString.length - 1
                        result += percentageString
                    }

                    VELOCITY -> {
                        val velocityString = note.velocity.toString()
                        skip = velocityString.length - 1
                        result += velocityString
                    }

                    MIDI_PITCH -> {
                        val midiPitchString = note.midiPitch.toString()
                        skip = midiPitchString.length - 1
                        result += midiPitchString
                    }

                    CHANNEL -> {
                        val channelString = note.channel.toString()
                        skip = channelString.length - 1
                        result += channelString
                    }
                }
            }
            result
        }
        for ((midiLoopIndex, midiLoopAsString) in midiLoopsAsStrings.withIndex()) {
            printAt(midiLoopIndex  + 1, 0, midiLoopAsString)
        }
        printSelectedNote()
    }

    fun update() {
        if (lastWidth != terminal.width || lastHeight != terminal.height) {
            reset()
        }
        lastWidth = terminal.width
        lastHeight = terminal.height

        printTopLine()
        printBottomLine()
        for ((midiLoopIndex, midiLoop) in loops.withIndex()) {
            val midiLoopAsString = midiLoopsAsStrings[midiLoopIndex]

            resetUnderline()
            printAt(midiLoopIndex + 1, midiLoop.previousTick, midiLoopAsString[midiLoop.previousTick].toString())

            setUnderline()
            printAt(midiLoopIndex + 1, midiLoop.currentTick, midiLoopAsString[midiLoop.currentTick].toString())
            resetUnderline()

        }
        printSelectedNote()
    }

    fun displayPercentages() {
        displayMode = PERCENTAGE
        reset()
    }

    fun displayNoteNames() {
        displayMode = NOTE_NAME
        reset()
    }

    fun displayVelocities() {
        displayMode = VELOCITY
        reset()
    }

    fun displayMidiPitch() {
        displayMode = MIDI_PITCH
        reset()
    }

    fun displayChannels() {
        displayMode = CHANNEL
        reset()
    }

    fun incSelectedNote() {
        val newNoteIndex = selectedNoteIndex + 1
        if (newNoteIndex >= selectedLoop.notesList.size) {
            return
        }
        resetSelectedNoteColor()
        selectedNoteIndex = newNoteIndex
        selectedNote = selectedLoop.notesList[newNoteIndex]
        printSelectedNote()
    }

    fun decSelectedNote() {
        val newNoteIndex = selectedNoteIndex - 1
        if (newNoteIndex < 0) {
            return
        }
        resetSelectedNoteColor()
        selectedNoteIndex = newNoteIndex
        selectedNote = selectedLoop.notesList[selectedNoteIndex]
        printSelectedNote()
    }

    fun incSelectedLoop() {
        val newLoopIndex = selectedLoopIndex + 1
        if (newLoopIndex >= loops.size) {
            return
        }
        resetSelectedNoteColor()
        selectedLoopIndex = newLoopIndex
        selectedLoop = loops[newLoopIndex]
        selectedNoteIndex = selectedNoteIndex.coerceIn(0, selectedLoop.notesList.size - 1)
        selectedNote = selectedLoop.notesList[selectedNoteIndex]
        printSelectedNote()
    }

    fun decSelectedLoop() {
        val newLoopIndex = selectedLoopIndex - 1
        if (newLoopIndex < 0) {
            return
        }
        resetSelectedNoteColor()
        selectedLoopIndex = newLoopIndex
        selectedLoop = loops[newLoopIndex]
        selectedNoteIndex = selectedNoteIndex.coerceIn(0, selectedLoop.notesList.size - 1)
        selectedNote = selectedLoop.notesList[selectedNoteIndex]
        printSelectedNote()
    }

    fun increaseSelectedNote() {
        when (displayMode) {
            NOTE_NAME -> selectedNote.increasePitch()
            PERCENTAGE -> selectedNote.increaseChance()
            VELOCITY -> selectedNote.increaseVelocity()
            MIDI_PITCH -> selectedNote.increasePitch()
            CHANNEL -> selectedNote.increaseChannel()
        }
        reset()
    }

    fun decreaseSelectedNote() {
        when (displayMode) {
            NOTE_NAME -> selectedNote.decreasePitch()
            PERCENTAGE -> selectedNote.decreaseChance()
            VELOCITY -> selectedNote.decreaseVelocity()
            MIDI_PITCH -> selectedNote.decreasePitch()
            CHANNEL -> selectedNote.decreaseChannel()
        }
        reset()
    }

    private fun printTopLine() {
        val inputDevice = if (inputDeviceFullName == null) "" else " inputDevice=${inputDeviceFullName}"
        printAt(
            0,
            0,
            "bpm=${"%.0f".format(bpmProvider())} outputDevice=${outputDeviceFullName}$inputDevice clockMode=${clockMode}"
        )
        eraseUntilEndOfLine()
        println()
    }

    private fun printBottomLine() {
        val percentageString = "%.0f".format(selectedNote.percentage * 100) + "%"
        terminal.printAtBottomLine("${SET_UNDERLINE}n${RESET_UNDERLINE}ame=${noteNameMask[selectedNote.midiPitch]} " +
                "${SET_UNDERLINE}m${RESET_UNDERLINE}idiPitch=${selectedNote.midiPitch} " +
                "${SET_UNDERLINE}v${RESET_UNDERLINE}elocity=${selectedNote.velocity} " +
                "${SET_UNDERLINE}p${RESET_UNDERLINE}ercentage=$percentageString")
    }

    private fun printSelectedNote() {
        val charToHighlight = midiLoopsAsStrings[selectedLoopIndex][selectedNote.startIndex]
        setRed()
        printAt(selectedLoopIndex + 1, selectedNote.startIndex, "$charToHighlight")
        resetColor()
    }

    private fun resetSelectedNoteColor() {
        val charToHighlight = midiLoopsAsStrings[selectedLoopIndex][selectedNote.startIndex]
        printAt(selectedLoopIndex + 1, selectedNote.startIndex, "$charToHighlight")
    }

    private enum class State {
        NOTE_NAME, PERCENTAGE, VELOCITY, MIDI_PITCH, CHANNEL
    }
}
