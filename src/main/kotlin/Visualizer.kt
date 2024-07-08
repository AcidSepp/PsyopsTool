package org.example

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color.GREEN
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.utils.ScreenUtils
import java.lang.Math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

val NOTE_NAME_TEXTURES = mapOf(
    "C" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/C.png")),
    "C#" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/C#.png")),
    "D" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/D.png")),
    "D#" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/D#.png")),
    "E" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/E.png")),
    "F" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/F.png")),
    "F#" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/F#.png")),
    "G" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/G.png")),
    "G#" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/G#.png")),
    "A" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/A.png")),
    "A#" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/A#.png")),
    "B" to Texture(FileHandle("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/B.png"))
)

class VisualiserCanvas(val midiLoops: List<MidiLoop>) : ApplicationAdapter() {

    private var width = 1000f
    private var height = 1000f

    private var shapeRenderer: ShapeRenderer? = null
    private var spriteBatch: SpriteBatch? = null

    override fun create() {
        shapeRenderer = ShapeRenderer().apply {
            setAutoShapeType(true)
        }
        spriteBatch = SpriteBatch()
    }

    override fun resize(width: Int, height: Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()

        shapeRenderer = ShapeRenderer().apply {
            setAutoShapeType(true)
        }
        spriteBatch = SpriteBatch()
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        val shapeRenderer = shapeRenderer!!
        shapeRenderer.begin()

        for ((index, midiLoop) in midiLoops.withIndex()) {
            shapeRenderer.set(Filled)

            midiLoop.loop //
                .forEach { //
                    val progress: Double = it.key.toDouble() / midiLoop.amountTicks.toDouble()
                    val posX = cos(-progress * 2 * PI + (PI / 2)) * (width * getCircleRadius(index))
                    val posY = sin(-progress * 2 * PI + (PI / 2)) * (height * getCircleRadius(index))

                    val spriteBatch = spriteBatch!!
                    val texture = NOTE_NAME_TEXTURES[it.value.noteName]!!
                    spriteBatch.begin()
                    val sprite = Sprite(texture)
                    sprite.setSize(40f, 40f)
                    sprite.setPositionMidHandled(posX.toFloat() + width / 2, posY.toFloat() + height / 2f)
                    sprite.setAlpha(it.value.chance)
                    sprite.draw(spriteBatch)
                    spriteBatch.end()
                }

            // draw position indicator
            val progress: Double = midiLoop.index / midiLoop.amountTicks.toDouble()
            val posX = cos(-progress * 2 * PI + (PI / 2)) * (width * getCircleRadius(index))
            val posY = sin(-progress * 2 * PI + (PI / 2)) * (height * getCircleRadius(index))
            shapeRenderer.color = RED
            shapeRenderer.circle(posX.toFloat() + width / 2, posY.toFloat() + height / 2, 20f)

            // draw helper circles
            shapeRenderer.color = GREEN
            shapeRenderer.set(Line)
            shapeRenderer.ellipse(
                -getCircleRadius(index) * width + width / 2,
                -getCircleRadius(index) * height + height / 2,
                getCircleRadius(index) * width * 2f,
                getCircleRadius(index) * height * 2f
            )

            drawCurrentNote(midiLoop, index, shapeRenderer)
        }
        shapeRenderer.end()
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
        val currentNote = midiLoop.currentNote
        if (currentNote != null) {
            val currentNoteProgress: Float =
                (midiLoop.index - currentNote.startIndex).toFloat() / currentNote.durationInTicks
            val currentNoteRadians: Float = currentNote.startIndex.toFloat() / midiLoop.amountTicks
            val currentNotePosX = cos(-currentNoteRadians * 2 * PI + (PI / 2)) * (width * getCircleRadius(index))
            val currentNotePosY = sin(-currentNoteRadians * 2 * PI + (PI / 2)) * (height * getCircleRadius(index))
            shapeRenderer.color = RED
            shapeRenderer.circle(
                currentNotePosX.toFloat() + width / 2,
                currentNotePosY.toFloat() + height / 2,
                50f * currentNoteProgress
            )
        }
    }
}

fun getCircleRadius(circleIndex: Int) = 0.4f * Math.pow(0.7, circleIndex.toDouble()).toFloat()

fun Sprite.setPositionMidHandled(posX: Float, posY: Float) =
    setPosition(posX - width / 2, posY - height / 2)

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
