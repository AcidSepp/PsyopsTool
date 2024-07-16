import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.nio.file.Path

class FontDrawer(val spriteBatch: SpriteBatch) {

    private var fontTextures: Map<Char, Texture>

    init {
        val characters = ('A' .. 'Z') + ('0' .. '9') + '=' + '#' + '.'
        fontTextures = characters.associateWith {
            val resource = javaClass.getResource("/char_$it.png")
            val path = Path.of(resource!!.toURI())
            Texture(FileHandle(path.toFile()))
        }
    }

    fun dispose() {
        fontTextures.values.forEach(Texture::dispose)
    }

    fun drawString(string: String, x: Float, y: Float, size: Float, offsetX: Float = 0f, offsetY: Float = 0f) {
        string.lines().withIndex().forEach { lineWithIndex ->
            val linePosY = -lineWithIndex.index * size
            lineWithIndex.value.withIndex().forEach { charWithIndex ->
                val texture = fontTextures[charWithIndex.value.uppercaseChar()]
                if (texture != null) {
                    val characterPosX = charWithIndex.index * size
                    spriteBatch.begin()
                    val sprite = Sprite(texture)
                    sprite.setSize(size, size)
                    sprite.setPosition(characterPosX + x + offsetX, linePosY + y + offsetY)
                    sprite.draw(spriteBatch)
                    spriteBatch.end()
                }
            }
        }
    }
}

fun FontDrawer.drawStringMidHandled(string: String, x: Float, y: Float, size: Float) {
    val height = string.lines().size * size
    val width = string.lines().maxBy { it.length }.length * size
    val offsetX = -width / 2f
    val offsetY = (height / 2f) - size
    drawString(string, x, y, size, offsetX, offsetY)
}

fun FontDrawer.drawStringVerticallyMidHandled(string: String, x: Float, y: Float, size: Float) {
    val height = string.lines().size * size
    val offsetY = (height / 2f) - size
    drawString(string, x, y, size, 0f, offsetY)
}
