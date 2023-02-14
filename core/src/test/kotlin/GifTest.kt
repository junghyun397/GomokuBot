import core.assets.Notation
import core.interact.message.graphics.ImageBoardRenderer
import org.junit.Test

class GifTest {

    @Test
    fun gifTest() {
        val seq = Notation.BoardIOInstance.buildPosSequence("a1b1c1d1e1f1g1h1i1j1").get()
        val his = buildList { seq.toList().foreach { add(it) } }
        val start = System.currentTimeMillis()

        val image = ImageBoardRenderer.renderHistoryAnimation(his)

        val finish = System.currentTimeMillis() - start

        println(finish)

        println(image.readAllBytes().last())
    }

}
