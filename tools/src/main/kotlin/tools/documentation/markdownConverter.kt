package tools.documentation

import utils.lang.tuple
import java.io.File

fun main() {
    val documentSource = File("./documents/").listFiles { _, name ->
        name.matches(Regex("about-renju-[a-z]{3}.md"))
    }!!.associate { it.name.dropLast(3).takeLast(3).uppercase() to it.readText() }

    val targetSource = File("./core/src/main/kotlin/core/interact/i18n/").listFiles { _, name ->
        name.matches(Regex("Language[A-Z]{3}.kt"))
    }!!.associate { it.name.dropLast(3).takeLast(3).uppercase() to it.readText() }

    var generateImage = true

    documentSource.keys.intersect(targetSource.keys).forEach { languageName ->
        val document = documentSource[languageName]!!
        val target = targetSource[languageName]!!

        val result = Regex("```(\n|.)*?```")
            .findAll(document)
            .map { matchResult ->
                val value = matchResult.value

                val imageStrings = matchResult.value
                    .drop(3)
                    .takeWhile { it != '\n' }
                    .split(",")
                    .map { it.split("=")[1].trim() }
                    .also { (fName, enableForbiddenPoints, lastMove) ->
                        if (generateImage) generateImage(
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
            .fold(document) { acc, (target, toBe) -> acc.replace(target, toBe) }

        generateImage = false

        val divider = "    override fun aboutRenjuDocument() = \"\"\""

        File("./core/src/main/kotlin/core/interact/i18n/Language$languageName.kt")
            .writeText("${target.substringBefore(divider)}$divider\n$result\"\"\".trimIndent()\n}")
    }

}
