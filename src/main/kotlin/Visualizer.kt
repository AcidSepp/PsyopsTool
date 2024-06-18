package org.example

import java.awt.*
import java.lang.Math.*
import javax.sound.midi.ShortMessage.NOTE_ON
import kotlin.math.cos
import kotlin.math.sin

class VisualiserCanvas(val midiLoop: MidiLoop) : Canvas() {

    init {
        background = Color.BLACK
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.color = Color.GREEN

        g.drawOval(100, 100, 800, 800)

        midiLoop.loop //
            .filter { it.value.command == NOTE_ON } //x
            .forEach { //
                val progress: Double = it.key.toDouble() / midiLoop.amountTicks.toDouble()
                val posX = cos(progress * 2 * PI - (PI / 2)) * 400
                val posY = sin(progress * 2 * PI - (PI / 2)) * 400

                g.fillOval(posX.toInt() + size.width / 2, posY.toInt() + size.height / 2, 20, 20)
            }

        val progress: Double = midiLoop.index / midiLoop.amountTicks.toDouble()
        val posX = cos(progress * 2 * PI - (PI / 2)) * 400
        val posY = sin(progress * 2 * PI - (PI / 2)) * 400

        g.color = Color.RED
        g.fillOval(posX.toInt() + size.width / 2, posY.toInt() + size.height / 2, 20, 20)
    }
}

class MidiLoopVisualiser(midiLoop: MidiLoop) : Frame() {

    private val visualiserCanvas = VisualiserCanvas(midiLoop)

    init {
        size = Dimension(1000, 1000)
        title = "Draw Rectangle"
        add(visualiserCanvas)
        isVisible = true
        pack()
    }

    override fun repaint() {
        super.repaint()
        visualiserCanvas.repaint()
    }
}
