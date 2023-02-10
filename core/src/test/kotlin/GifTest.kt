import com.sksamuel.scrimage.AwtImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import core.assets.AnimatedGifEncoder
import core.assets.Notation
import core.interact.message.graphics.ImageBoardRenderer
import org.junit.Test
import renju.Board
import renju.notation.Pos
import utils.structs.Option
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.time.Duration

internal class GifTest {

    @Test
    fun test() {
        image()
        image()
    }

    fun image() {
        val history = Notation.BoardIOInstance.buildPosSequence("a1a2a3a4a5a6a7a8a9a10a11a12a13a14a15b1b2b3b4b5b6b7b8b9b10b11b12b13b14b15")
            .get()
            .foldLeft(listOf<Pos>()) { acc, pos -> acc + pos }

        val start = System.currentTimeMillis()

        val file = File("/home/junghyun397/a.gif")
        file.createNewFile()
        val out = FileOutputStream(file)

        AnimatedGifEncoder().apply {
            setSize(ImageBoardRenderer.DIMENSION.width, ImageBoardRenderer.DIMENSION.height)
            setRepeat(1)
            setQuality(1)
            start(out)
            setDelay(1000)

            var board: Board = Notation.EmptyBoard

            history.forEachIndexed { index, pos ->
                board = board.makeMove(pos)
                addFrame(ImageBoardRenderer.renderBufferedImage(board, Option.Some(history.subList(0, index + 1))))
            }

            finish()
        }

        val age = System.currentTimeMillis() - start

        val start2 = System.currentTimeMillis()

        val file2 = File("/home/junghyun397/b.gif")
        file2.createNewFile()
        val out2 = FileOutputStream(file2)

        val gifBuilder = StreamingGifWriter(Duration.ofSeconds(1), false, false)

        gifBuilder.prepareStream(out2, BufferedImage.TYPE_INT_ARGB).apply {
            var board: Board = Notation.EmptyBoard

            history.forEachIndexed { index, pos ->
                board = board.makeMove(pos)
                writeFrame(AwtImage(ImageBoardRenderer.renderBufferedImage(board, Option.Some(history.subList(0, index + 1)))).toImmutableImage())
            }

            close()
        }

        val gib = System.currentTimeMillis() - start2

        println("$age $gib")
    }

}
