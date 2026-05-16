package utils.lang

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.imageio.ImageIO
import kotlin.time.Instant

fun BufferedImage.clone(): BufferedImage =
    BufferedImage(this.colorModel, this.copyData(null), this.colorModel.isAlphaPremultiplied, null)

fun BufferedImage.toInputStream(): InputStream {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "png", outputStream)
    return ByteArrayInputStream(outputStream.toByteArray())
}

fun String.toInputStream(): InputStream = this.byteInputStream(Charsets.UTF_8)

fun Iterable<Char>.asString(): String = String(this.toList().toCharArray())

fun LocalDateTime.toUtcInstant(): Instant = Instant.fromEpochMilliseconds(this.toInstant(ZoneOffset.UTC).toEpochMilli())
