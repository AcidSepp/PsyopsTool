package de.yw.psyops

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

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
                        'p' -> printer.displayPercentages()
                    }
                }
            }
        }
    }
}