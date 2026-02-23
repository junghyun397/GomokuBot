package tools.documentation

import renju.notation.Pos
import java.nio.file.Paths

private fun main() {
    val workingDir = Paths.get("").toAbsolutePath()
    println("fPath: $workingDir/")

    val fPath = readln()

    while (true) {
        print("board: ")
        val boardSource = (0 until Pos.BOARD_WIDTH + 2)
            .joinToString { readln() }

        print("lastMove: ")
        val lastMove = readln().let { if (it == "") "a1" else it }

        print("fileName: ")
        val fName = readln().let { if (it == "") "unnamed" else it }

        print("enableForbiddenPoints: ")
        val enableForbiddenPoints = readln().let { if (it == "") "true" else it }.toBooleanStrict()

        generateImage(boardSource, lastMove, fName, fPath, enableForbiddenPoints)
    }
}
