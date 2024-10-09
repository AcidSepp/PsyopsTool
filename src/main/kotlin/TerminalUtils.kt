// https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797

private const val ESC = "\u001B"

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

fun setUnderline() = print("$ESC[4m")

fun resetUnderline() = print("$ESC[24m")