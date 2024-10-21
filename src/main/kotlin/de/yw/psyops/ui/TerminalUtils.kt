package de.yw.psyops.ui

import org.jline.terminal.Terminal

// https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797

const val ESC = '\u001B'

const val ANSI_RESET = "$ESC[0m"
const val ANSI_BLACK = "$ESC[30m"
const val ANSI_RED = "$ESC[31m"
const val ANSI_GREEN = "$ESC[32m"
const val ANSI_YELLOW = "$ESC[33m"
const val ANSI_BLUE = "$ESC[34m"
const val ANSI_PURPLE = "$ESC[35m"
const val ANSI_CYAN = "$ESC[36m"
const val ANSI_WHITE = "$ESC[37m"

const val SET_UNDERLINE = "$ESC[4m"
const val RESET_UNDERLINE = "$ESC[24m"

fun erase() = print("\r$ESC[2J")

fun moveCursor(row: Int, column: Int) =
    print("$ESC[${row + 1};${column + 1}H")

fun clearLine() = print("\r$ESC[2K")

fun printAt(row: Int, column: Int, string: String) {
    moveCursor(row, column)
    print(string)
}

fun eraseUntilEndOfLine() = print("$ESC[0K")

fun makeCursorInvisible() = print("$ESC[?25l")

fun makeCursorVisible() = print("$ESC[?25h")

fun moveCursorLeft(count: Int) = print("$ESC[${count}D")

fun setUnderline() = print(SET_UNDERLINE)

fun resetUnderline() = print(RESET_UNDERLINE)

fun disableLineWrapping() = print("$ESC[?7l")

fun enableLineWrapping() = print("$ESC[?7h")

fun setRed() = print(ANSI_RED)

fun resetColor() = print(ANSI_RESET)

fun Terminal.printAtBottomLine(string: String) {
    printAt(height, 0, string)
}