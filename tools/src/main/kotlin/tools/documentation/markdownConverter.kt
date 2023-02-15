package tools.documentation

import utils.lang.tuple

fun main() {
    print("source: ")
    val source = generateSequence(::readLine)
        .takeWhile { it != "end$" }
        .joinToString("\n")

    val result = Regex("```(\n|.)*?```")
        .findAll(source)
        .map { matchResult ->
            val value = matchResult.value

            val imageStrings = matchResult.value
                .drop(3)
                .takeWhile { it != '\n' }
                .split(",")
                .map { it.split("=")[1].trim() }
                .also { (fName, enableForbiddenPoints, lastMove) ->
                    generateImage(
                        matchResult.value,
                        lastMove,
                        fName,
                        "images",
                        enableForbiddenPoints.toBooleanStrict()
                    )
                }
                .first()
                .let { "![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/$it.png)" }

            tuple(value, imageStrings)
        }
        .fold(source) { acc, (target, toBe) -> acc.replace(target, toBe) }
        .dropLast(5)

    println(result)
}
