package tools.documentation

import jrenju.notation.Renju
import java.nio.file.Paths

private fun main() {
    val workingDir = Paths.get("").toAbsolutePath()
    println("fPath: $workingDir/")

    val fPath = readLine()!!

    while (true) {
        print("board: ")
        val boardSource = (0 until Renju.BOARD_WIDTH() + 2)
            .joinToString { readLine()!! }

        print("lastMove: ")
        val lastMove = readLine()!!.let { if (it == "") "a1" else it }

        print("fileName: ")
        val fName = readLine()!!.let { if (it == "") "unnamed" else it }

        print("enableForbiddenPoints: ")
        val enableForbiddenPoints = readLine()!!.let { if (it == "") "true" else it }.toBooleanStrict()

        generateImage(boardSource, lastMove, fName, fPath, enableForbiddenPoints)
    }
}
