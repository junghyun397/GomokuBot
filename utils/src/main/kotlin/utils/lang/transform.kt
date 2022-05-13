package utils.lang

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

fun BufferedImage.clone(): BufferedImage =
    BufferedImage(this.colorModel, this.copyData(null), this.colorModel.isAlphaPremultiplied, null)

fun BufferedImage.toInputStream(): InputStream {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "png", outputStream)
    return ByteArrayInputStream(outputStream.toByteArray())
}

fun String.toInputStream(): InputStream = this.byteInputStream(Charsets.UTF_8)

fun CharArray.asString(): String = String(this)
