package tools.documentation

import core.interact.message.graphics.HistoryRenderType
import core.interact.message.graphics.ImageBoardRenderer
import renju.BoardIO
import renju.notation.Pos
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun generateImage(boardSource: String, lastMove: String, fName: String, fPath: String, enableForbiddenPoints: Boolean) {
    val board = BoardIO.fromBoardText(
        boardSource,
        Pos.fromCartesian(if (lastMove == "null") "a1" else lastMove).get().idx()
    ).get()

    val inputStream = ImageBoardRenderer.renderInputStream(
        board,
        emptyList(),
        if (lastMove == "null") HistoryRenderType.LAST else HistoryRenderType.SEQUENCE,
        null,
        null,
        enableForbiddenPoints,
    )

    val tempFile = File(fPath, "$fName.png")

    println(tempFile.absolutePath)
    tempFile.createNewFile()

    Files.copy(
        inputStream,
        tempFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING
    )
}
