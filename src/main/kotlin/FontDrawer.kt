import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.nio.file.Path

class FontDrawer {

    private var fontTextures: Map<Char, Texture>

    init {
        val characters = ('A'..'Z') + ('0'..'9')
        fontTextures = characters.associateWith {
            val resource = javaClass.getResource("/char_$it.png")
            val path = Path.of(resource!!.toURI())
            Texture(FileHandle(path.toFile()))
        }
    }

    fun dispose() {
        fontTextures.values.forEach(Texture::dispose)
    }

    fun drawStringMidHandled(string: String, spriteBatch: SpriteBatch, x: Float, y: Float, size: Float) {
        val height = string.lines().size * size
        val width = string.lines().maxBy { it.length }.length * size

        string.lines().withIndex().forEach { lineWithIndex ->

            var linePosY = - lineWithIndex.index * size

            lineWithIndex.value.withIndex().forEach { charWithIndex ->
                var characterPosX = charWithIndex.index * size

                spriteBatch.begin()
                val sprite = Sprite(fontTextures[charWithIndex.value.uppercaseChar()]!!)
                sprite.setSize(size, size)
                sprite.setPosition(characterPosX + x - width / 2f, linePosY + y + (height / 2f) - size)
                sprite.draw(spriteBatch)
                spriteBatch.end()
            }

        }
    }
}