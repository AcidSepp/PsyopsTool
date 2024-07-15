import java.awt.Font
import java.awt.FontMetrics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun generateCharacterBitmaps() {
    val characters = ('A' .. 'Z') + ('0' .. '9')
    val font = Font("Joystix", Font.PLAIN, 64)
    val imageSize = 64
    val imageType = BufferedImage.TYPE_INT_ARGB

    for (char in characters) {
        val image = BufferedImage(imageSize, imageSize, imageType)

        val graphics = image.createGraphics()

        graphics.font = font
        val metrics: FontMetrics = graphics.fontMetrics
        val x = (imageSize - metrics.charWidth(char)) / 2
        val y = ((imageSize - metrics.height) / 2) + metrics.ascent
        graphics.drawString(char.toString(), x, y)

        graphics.dispose()

        val outputFile = File("/Users/yannick/IdeaProjects/PsyopsTool/src/main/resources/char_${char}.png")
        ImageIO.write(image, "png", outputFile)
    }
}

fun main() {
    generateCharacterBitmaps()
}
