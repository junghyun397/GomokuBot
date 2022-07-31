package tools.documentation

import core.interact.message.graphics.ImageBoardRenderer
import jrenju.`BoardIO$`
import jrenju.notation.Pos
import utils.structs.Option
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun generateImage(boardSource: String, lastMove: String, fName: String, fPath: String, enableForbiddenPoints: Boolean) {
    val board = `BoardIO$`.`MODULE$`.fromBoardText(
        boardSource,
        Pos.fromCartesian(if (lastMove == "null") "a1" else lastMove).get().idx()
    ).get()

    val (inputStream, _) = ImageBoardRenderer.renderImageBoard(
        board,
        if (lastMove == "null") Option(emptyList()) else Option.Empty,
        enableForbiddenPoints
    )

    val tempFile = File(fPath, "$fName.png")
        .also { println(it.absolutePath) }
        .also { it.createNewFile() }

    Files.copy(
        inputStream,
        tempFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING
    )
}