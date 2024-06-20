package org.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener
import com.badlogic.gdx.graphics.Color.GREEN
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.utils.ScreenUtils
import java.lang.Math.PI
import javax.sound.midi.ShortMessage.NOTE_ON
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

class VisualiserCanvas(val midiLoop: MidiLoop) : ApplicationAdapter() {

    private var width = 1000
    private var height = 1000

    override fun create() {
    }

    override fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.translate(width / 2f, height / 2f, 0f)

        shapeRenderer.begin(Filled)
        shapeRenderer.color = GREEN

        midiLoop.loop //
            .filter { it.value.command == NOTE_ON } //x
            .forEach { //
                val progress: Double = it.key.toDouble() / midiLoop.amountTicks.toDouble()
                val posX = cos(progress * 2 * PI - (PI / 2)) * (width * 0.4f)
                val posY = sin(progress * 2 * PI - (PI / 2)) * (height * 0.4f)

                shapeRenderer.circle(posX.toFloat(), posY.toFloat(), 20f)
            }

        val progress: Double = midiLoop.index / midiLoop.amountTicks.toDouble()
        val posX = cos(progress * 2 * PI - (PI / 2)) * (width * 0.4f)
        val posY = sin(progress * 2 * PI - (PI / 2)) * (height * 0.4f)

        shapeRenderer.color = RED
        shapeRenderer.circle(posX.toFloat(), posY.toFloat(), 20f)

        shapeRenderer.end()
        shapeRenderer.dispose()
    }
}

class MidiLoopVisualiser(midiLoop: MidiLoop) {

    init {
        Thread {
            val config = Lwjgl3ApplicationConfiguration()
            config.setForegroundFPS(60)
            config.setTitle("My GDX Game")
            config.setResizable(true)
            config.setWindowedMode(1000, 1000)
            config.setWindowListener(object : Lwjgl3WindowListener {
                override fun created(p0: Lwjgl3Window?) {
                }

                override fun iconified(p0: Boolean) {
                }

                override fun maximized(p0: Boolean) {
                }

                override fun focusLost() {
                }

                override fun focusGained() {
                }

                override fun closeRequested(): Boolean {
                    exitProcess(0)
                }

                override fun filesDropped(p0: Array<out String>?) {
                }

                override fun refreshRequested() {
                }
            })
            Lwjgl3Application(VisualiserCanvas(midiLoop), config)
        }.start()
    }
}
