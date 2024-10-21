package de.yw.psyops.masks

val NOTE_NAMES = arrayOf(
    "C",
    "C#",
    "D",
    "D#",
    "E",
    "F",
    "F#",
    "G",
    "G#",
    "A",
    "A#",
    "B"
)

object GenericKeyboardMask : NoteNameMask {
    override fun get(midiPitch: Int) =
        NOTE_NAMES[midiPitch % NOTE_NAMES.size]
}