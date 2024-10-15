package de.yw.psyops

import de.yw.psyops.Printer.State.*
import org.jline.terminal.Terminal
import java.util.*

class Printer(
    private val loops: List<MidiLoop>,
    private val bpmProvider: () -> Float,
    private val inputDeviceFullName: String?,
    private val outputDeviceFullName: String,
    private val clockMode: ClockMode,
    private val terminal: Terminal
) {

    @Volatile
    private var display = NOTE_NAME
    private var midiLoopsAsStrings = listOf<String>()

    private var lastWidth = 0
    private var lastHeight = 0

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            makeCursorVisible()
            enableLineWrapping()
        })
    }

    fun displayPercentages() {
        display = PERCENTAGE
        reset()
    }

    fun displayNoteNames() {
        display = NOTE_NAME
        reset()
    }

    fun displayVelocities() {
        display = VELOCITY
        reset()
    }

    fun displayMidiPitch() {
        display = MIDI_NOTE
        reset()
    }

    fun displayChannels() {
        display = CHANNEL
        reset()
    }

    fun reset() {
        Locale.setDefault(Locale.US)
        erase()
        disableLineWrapping()
        makeCursorInvisible()
        printTopLine()
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

                if (note != null) {
                    lastPrintedNote = note
                    when (display) {
                        NOTE_NAME -> {
                            skip = note.name.length - 1
                            result += note.name
                        }

                        PERCENTAGE -> {
                            val chanceString = "%.0f".format(note.chance * 100) + "%"
                            skip = chanceString.length - 1
                            result += chanceString
                        }

                        VELOCITY -> {
                            val velocityString = note.velocity.toString()
                            skip = velocityString.length - 1
                            result += velocityString
                        }

                        MIDI_NOTE -> {
                            val midiPitchString = note.note.toString()
                            skip = midiPitchString.length - 1
                            result += midiPitchString
                        }

                        CHANNEL -> {
                            val channelString = note.channel.toString()
                            skip = channelString.length - 1
                            result += channelString
                        }
                    }
                } else {
                    if (lastPrintedNote != null) {
                        result += if (lastPrintedNote.containsTick(tickIndex)) {
                            "•"
                        } else {
                            "·"
                        }
                    }
                }
            }
            return@map result
        }
        midiLoopsAsStrings.forEach(::println)
    }

    fun update() {
        if (lastWidth != terminal.width || lastHeight != terminal.height) {
            reset()
        }
        lastWidth = terminal.width
        lastHeight = terminal.height

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
        val inputDevice = if (inputDeviceFullName == null) "" else "inputDevice=${inputDeviceFullName}"
        printAt(
            0,
            0,
            "bpm=${"%.0f".format(bpmProvider())} outputDevice=${outputDeviceFullName}$inputDevice clockMode=${clockMode}"
        )
        eraseUntilEndOfLine()
        println()
    }

    private enum class State {
        NOTE_NAME,
        PERCENTAGE,
        VELOCITY,
        MIDI_NOTE,
        CHANNEL
    }
}
