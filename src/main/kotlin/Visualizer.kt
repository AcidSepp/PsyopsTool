package org.example

import FontDrawer
import NOTE_NAMES
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
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
import drawStringVerticallyMidHandled
import java.lang.Math.PI
import java.nio.file.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

class Visualizer(private val midiLoops: List<MidiLoop>) : ApplicationAdapter() {

    private lateinit var noteNameTextures: Map<String, Texture>
    private var debug = false

    private var width = 1000f
    private var height = 1000f
    private val circleSize = 0.08f

    private var shapeRenderer: ShapeRenderer? = null
    private var spriteBatch: SpriteBatch? = null
    private var fontDrawer: FontDrawer? = null

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
        Lwjgl3Application(this, config)
    }

    override fun create() {
        noteNameTextures = NOTE_NAMES.associateWith {
            val resource = javaClass.getResource("/$it.png")
            val path = Path.of(resource!!.toURI())
            Texture(FileHandle(path.toFile()))
        }
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun keyTyped(character: Char): Boolean {
                if (character == 'd') {
                    debug = !debug;
                }
                return true
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()

        shapeRenderer = ShapeRenderer().apply {
            setAutoShapeType(true)
            transformMatrix.scale(this@Visualizer.width / 2, this@Visualizer.height / 2, 0f)
            transformMatrix.translate(1f, 1f, 0f)
        }
        spriteBatch = SpriteBatch().apply {
            transformMatrix.scale(this@Visualizer.width / 2, this@Visualizer.height / 2, 0f)
            transformMatrix.translate(1f, 1f, 0f)

            fontDrawer = FontDrawer(this)
        }
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        val shapeRenderer = shapeRenderer!!
        val spriteBatch = spriteBatch!!
        val fontDrawer = fontDrawer!!

        for ((index, midiLoop) in midiLoops.withIndex()) {
            renderUsage(fontDrawer)
            renderCurrentNotePlayingIndicator(midiLoop, index, shapeRenderer)
            renderHelperCircle(shapeRenderer, index)
            renderAllNotesInLoop(midiLoop, index, shapeRenderer, spriteBatch, fontDrawer)
            renderPositionIndicator(midiLoop, index, shapeRenderer)
        }
    }

    override fun dispose() {
        super.dispose()
        shapeRenderer?.dispose()
        spriteBatch?.dispose()
        fontDrawer?.dispose()
    }

    private fun renderUsage(fontDrawer: FontDrawer) {
        fontDrawer.drawStringVerticallyMidHandled("D: debug", -1f + circleSize / 2f, -1f + circleSize / 2f, circleSize / 4)
    }

    private fun renderCurrentNotePlayingIndicator(
        midiLoop: MidiLoop,
        index: Int,
        shapeRenderer: ShapeRenderer
    ) {
        shapeRenderer.begin()
        shapeRenderer.color = RED
        shapeRenderer.set(Line)
        val currentNote = midiLoop.currentNote
        if (currentNote != null) {
            val currentNoteProgress =
                (midiLoop.index - currentNote.startIndex).toFloat() / currentNote.durationInTicks
            val currentNoteRadians = currentNote.startIndex.toFloat() / midiLoop.amountTicks
            val currentNotePosX = cos(-currentNoteRadians * 2 * PI + (PI / 2)) * (getCircleRadius(index))
            val currentNotePosY = sin(-currentNoteRadians * 2 * PI + (PI / 2)) * (getCircleRadius(index))
            shapeRenderer.ellipseMidHandled(
                currentNotePosX.toFloat(),
                currentNotePosY.toFloat(),
                circleSize * currentNoteProgress + circleSize,
                circleSize * currentNoteProgress + circleSize
            )
        }
        shapeRenderer.end()
    }

    private fun renderHelperCircle(shapeRenderer: ShapeRenderer, index: Int) {
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
    }

    private fun renderAllNotesInLoop(
        midiLoop: MidiLoop,
        index: Int,
        shapeRenderer: ShapeRenderer,
        spriteBatch: SpriteBatch,
        fontDrawer: FontDrawer
    ) {
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

                if (debug) {
                    fontDrawer.drawStringVerticallyMidHandled(
                        it.value.toString(),
                        posX + circleSize / 1.5f,
                        posY,
                        circleSize / 4
                    )
                }
            }
    }

    private fun renderPositionIndicator(
        midiLoop: MidiLoop,
        index: Int,
        shapeRenderer: ShapeRenderer
    ) {
        val progress = midiLoop.index.toFloat() / midiLoop.amountTicks.toFloat()
        val posX = cos(-progress * 2 * PI + (PI / 2)) * getCircleRadius(index)
        val posY = sin(-progress * 2 * PI + (PI / 2)) * getCircleRadius(index)
        shapeRenderer.begin()
        shapeRenderer.color = RED
        shapeRenderer.set(Filled)
        shapeRenderer.ellipseMidHandled(posX.toFloat(), posY.toFloat(), circleSize, circleSize)
        shapeRenderer.end()
    }
}

private fun getCircleRadius(circleIndex: Int) = 0.9f - circleIndex * 0.1f

private fun Sprite.setPositionMidHandled(posX: Float, posY: Float) =
    setPosition(posX - width / 2, posY - height / 2)

private fun ShapeRenderer.ellipseMidHandled(posX: Float, posY: Float, width: Float, height: Float, segments: Int = 64) =
    ellipse(posX - width / 2, posY - height / 2, width, height, segments)
