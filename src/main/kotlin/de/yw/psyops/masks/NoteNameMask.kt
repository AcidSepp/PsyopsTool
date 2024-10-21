package de.yw.psyops.masks

interface NoteNameMask {
    operator fun get(midiPitch: Int): String
}

enum class NoteNameMasks(val mask: NoteNameMask) {
    GENERIC_KEYBOARD(GenericKeyboardMask),
    DRUMBRUTE_IMPACT(DrumBruteImpactMask)
}