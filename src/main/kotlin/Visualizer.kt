package org.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.utils.ScreenUtils
import java.lang.Math.PI
import java.nio.file.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

class VisualiserCanvas(private val midiLoops: List<MidiLoop>) : ApplicationAdapter() {

    private lateinit var noteNameTextures: Map<String, Texture>

    private var width = 1000f
    private var height = 1000f
    private val circleSize = 0.08f

    private var shapeRenderer: ShapeRenderer? = null
    private var spriteBatch: SpriteBatch? = null

    override fun create() {
        noteNameTextures = NOTE_NAMES.associateWith {
            val resource = javaClass.getResource("/$it.png")
            val path = Path.of(resource!!.toURI())
            Texture(FileHandle(path.toFile()))
        }
    }

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()

        shapeRenderer = ShapeRenderer().apply {
            setAutoShapeType(true)
            transformMatrix.scale(this@VisualiserCanvas.width / 2, this@VisualiserCanvas.height / 2, 0f)
            transformMatrix.translate(1f, 1f, 0f)
        }
        spriteBatch = SpriteBatch().apply {
            transformMatrix.scale(this@VisualiserCanvas.width / 2, this@VisualiserCanvas.height / 2, 0f)
            transformMatrix.translate(1f, 1f, 0f)
        }
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        val shapeRenderer = shapeRenderer!!
        val spriteBatch = spriteBatch!!

        for ((index, midiLoop) in midiLoops.withIndex()) {
            // draw helper circles
            shapeRenderer.begin()
            shapeRenderer.color = GREEN
            shapeRenderer.set(Line)
            val helperCircleRadius = getCircleRadius(index)
            shapeRenderer.ellipseMidHandled(
                0f,
                0f,
                helperCircleRadius * 2,
                helperCircleRadius * 2,
            )
            shapeRenderer.end()

            midiLoop.loop //
                .forEach { //
                    val progress = it.key.toFloat() / midiLoop.amountTicks.toFloat()
                    val posX = cos(-progress * 2 * PI + (PI / 2)).toFloat() * getCircleRadius(index)
                    val posY = sin(-progress * 2 * PI + (PI / 2)).toFloat() * getCircleRadius(index)

                    shapeRenderer.begin()
                    shapeRenderer.color = BLACK
                    shapeRenderer.set(Filled)
                    shapeRenderer.ellipseMidHandled(posX, posY, circleSize, circleSize)
                    shapeRenderer.end()

                    spriteBatch.begin()
                    val sprite = Sprite(noteNameTextures[it.value.noteName]!!)
                    sprite.setSize(circleSize, circleSize)
                    sprite.setPositionMidHandled(posX, posY)
                    sprite.setAlpha(it.value.chance)
                    sprite.draw(spriteBatch)
                    spriteBatch.end()
                }

            // draw position indicator
            val progress = midiLoop.index.toFloat() / midiLoop.amountTicks.toFloat()
            val posX = cos(-progress * 2 * PI + (PI / 2)) * getCircleRadius(index)
            val posY = sin(-progress * 2 * PI + (PI / 2)) * getCircleRadius(index)
            shapeRenderer.begin()
            shapeRenderer.color = RED
            shapeRenderer.set(Filled)
            shapeRenderer.ellipseMidHandled(posX.toFloat(), posY.toFloat(), circleSize, circleSize)
            shapeRenderer.end()

            drawCurrentNote(midiLoop, index, shapeRenderer)
        }
    }

    override fun dispose() {
        super.dispose()
        shapeRenderer?.dispose()
        spriteBatch?.dispose()
    }

    private fun drawCurrentNote(
        midiLoop: MidiLoop,
        index: Int,
        shapeRenderer: ShapeRenderer
    ) {
        shapeRenderer.begin()
        val currentNote = midiLoop.currentNote
        if (currentNote != null) {
            val currentNoteProgress =
                (midiLoop.index - currentNote.startIndex).toFloat() / currentNote.durationInTicks
            val currentNoteRadians = currentNote.startIndex.toFloat() / midiLoop.amountTicks
            val currentNotePosX = cos(-currentNoteRadians * 2 * PI + (PI / 2)) * (getCircleRadius(index))
            val currentNotePosY = sin(-currentNoteRadians * 2 * PI + (PI / 2)) * (getCircleRadius(index))
            shapeRenderer.color = RED
            shapeRenderer.ellipseMidHandled(
                currentNotePosX.toFloat(),
                currentNotePosY.toFloat(),
                2 * circleSize * currentNoteProgress,
                2 * circleSize * currentNoteProgress
            )
        }
        shapeRenderer.end()
    }
}

fun getCircleRadius(circleIndex: Int) = 0.9f - circleIndex * 0.1f

fun Sprite.setPositionMidHandled(posX: Float, posY: Float) =
    setPosition(posX - width / 2, posY - height / 2)

fun ShapeRenderer.ellipseMidHandled(posX: Float, posY: Float, width: Float, height: Float, segments: Int = 64) =
    ellipse(posX - width / 2, posY - height / 2, width, height, segments)

class MidiLoopVisualiser(midiLoop: List<MidiLoop>) {

    init {
        val config = Lwjgl3ApplicationConfiguration()
        config.setForegroundFPS(144)
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
    }
}
