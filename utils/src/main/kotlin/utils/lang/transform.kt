package utils.lang

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO

fun BufferedImage.clone(): BufferedImage =
    BufferedImage(this.colorModel, this.copyData(null), this.colorModel.isAlphaPremultiplied, null)

fun BufferedImage.toInputStream(): InputStream {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "png", outputStream)
    return ByteArrayInputStream(outputStream.toByteArray())
}

fun String.toInputStream(): InputStream = this.byteInputStream(Charsets.UTF_8)

fun Iterable<Char>.asString(): String = String(this.toList().toCharArray())

fun encodeBase64(source: ByteArray): String = String(Base64.getEncoder().encode(source))

fun decodeBase64(source: String): ByteArray = Base64.getDecoder().decode(source)
