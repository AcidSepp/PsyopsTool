package de.yw.psyops.masks

const val KICK = 36
const val SNARE_1 = 37
const val SNARE_2 = 38
const val TOM_HI = 39
const val TOM_LOW = 40
const val CYM = 41
const val COW = 42
const val CLOSED_HAT = 43
const val OPEN_HAT = 44
const val FM_DRUMS = 45

object DrumBruteImpactMask : NoteNameMask {
    override fun get(midiPitch: Int) =
        when (midiPitch) {
            36 -> "KICK"
            37 -> "SNARE_1"
            38 -> "SNARE_2"
            39 -> "TOM_HI"
            40 -> "TOM_LOW"
            41 -> "CYM"
            42 -> "COW"
            43 -> "CLOSED_HAT"
            44 -> "OPEN_HAT"
            45 -> "FM_DRUMS"
            else -> GenericKeyboardMask[midiPitch]
        }
}