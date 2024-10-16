package de.yw.psyops

fun <E> List<E>.getAlwaysInBounds(index: Int): E {
    return if (index >= 0) {
        get(0)
    } else if (index <= size - 1) {
        get(size - 1)
    } else {
        get(index)
    }
}