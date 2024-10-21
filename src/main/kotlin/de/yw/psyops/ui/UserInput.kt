package de.yw.psyops.ui

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jline.terminal.Terminal

class UserInput(private val printer: Printer, private val terminal: Terminal) {

    fun run() {
        runBlocking {
            terminal.enterRawMode()
            launch {
                while (isActive) {
                    val reader = terminal.reader()
                    when (reader.read().toChar()) {
                        'c' -> printer.displayChannels()
                        'n' -> printer.displayNoteNames()
                        'v' -> printer.displayVelocities()
                        'm' -> printer.displayMidiPitch()
                        'p' -> printer.displayProbabilitys()
                        's' -> printer.incSelectedLoop()
                        'w' -> printer.decSelectedLoop()
                        'a' -> printer.decSelectedNote()
                        'd' -> printer.incSelectedNote()
                        '+' -> printer.increaseSelectedNote()
                        '-' -> printer.decreaseSelectedNote()
                        ESC -> {
                            when (reader.read().toChar()) {
                                '[' -> {
                                    when (reader.read().toChar()) {
                                        'B' -> printer.incSelectedLoop()
                                        'A' -> printer.decSelectedLoop()
                                        'D' -> printer.decSelectedNote()
                                        'C' -> printer.incSelectedNote()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}