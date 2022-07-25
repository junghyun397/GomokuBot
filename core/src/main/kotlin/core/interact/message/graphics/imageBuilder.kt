package core.interact.message.graphics

import jrenju.`BoardIO$`
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.structs.Option
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private fun saveAsFile(boardSource: String, lastMove: String, fName: String, fPath: String, enableForbiddenPoints: Boolean) {
    val board = `BoardIO$`.`MODULE$`.fromBoardText(
        boardSource,
        Pos.fromCartesian(if (lastMove == "null") "a1" else lastMove).get().idx()
    ).get()

    val (file, _) = ImageBoardRenderer.renderImageBoard(
        board,
        if (lastMove == "null") Option(emptyList()) else Option.Empty,
        enableForbiddenPoints
    )

    val tempFile = File("$fPath/$fName.png")
        .also { it.createNewFile() }

    Files.copy(
        file,
        tempFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING
    )
}

private fun repl() {
    println("fPath: ")

    val fPath = readLine()!!

    while (true) {
        print("board: ")
        val boardSouce = (0 until Renju.BOARD_WIDTH() + 2)
            .joinToString { readLine()!! }

        print("lastMove: ")
        val lastMove = readLine()!!.let { if (it == "") "a1" else it }

        print("fileName: ")
        val fName = readLine()!!.let { if (it == "") "unnamed" else it }

        print("enableForbiddenPoints: ")
        val enableForbiddenPoints = readLine()!!.let { if (it == "") "true" else it }.toBooleanStrict()

        saveAsFile(boardSouce, lastMove, fName, fPath, enableForbiddenPoints)
    }
}

private fun parseMarkdown() {
    print("fPath: ")

    val fPath = readLine()!!

    print("source: ")

    val source = generateSequence(::readLine).takeWhile { it != "endinput$" }.joinToString("\n")

    Regex("```(\n|.)*?```")
        .findAll(source)
        .forEach { matchResult ->
            val (fName, enableForbiddenPoints, lastMove) = matchResult.value
                .drop(3)
                .takeWhile { it != '\n' }
                .split(",")
                .map { it.split("=")[1].trim() }

            saveAsFile(
                matchResult.value,
                lastMove,
                fName,
                fPath,
                enableForbiddenPoints.toBooleanStrict()
            )
        }
}

private fun main() {
    parseMarkdown()
//    repl()
}
