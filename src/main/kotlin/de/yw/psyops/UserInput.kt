package de.yw.psyops

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jline.terminal.TerminalBuilder

class UserInput(private val printer: Printer) {

    fun run() {
        runBlocking {
            val terminal = TerminalBuilder.builder().system(true).build()
            terminal.enterRawMode()
            launch {
                while (isActive) {
                    val reader = terminal.reader()
                    when (reader.read().toChar()) {
                        'c' -> printer.displayChances()
                        'n' -> printer.displayNoteNames()
                        'v' -> printer.displayVelocities()
                        'm' -> printer.displayMidiPitch()
                    }
                }
            }
        }
    }
}