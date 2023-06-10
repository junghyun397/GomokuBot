package utils.assets

import utils.lang.tuple
import utils.structs.Either

typealias SimplifiedMarkdownDocument = List<Pair<String, List<Pair<String?, List<Either<String, URL>>>>>>
typealias MarkdownAnchorMapping = Map<String, Int>

// ## h2Title
// ### h3Title
// ![](https://URL(ref))
// text
fun parseSimplifiedMarkdownDocument(source: String): Pair<SimplifiedMarkdownDocument, MarkdownAnchorMapping> {
    val anchorMapping = hashMapOf<String, Int>()

    val document = source.split("\n## ")
        .map { if (it.startsWith("## ")) it.drop(3).trim() else it.trim() }
        .withIndex()
        .map { (index, h2Elements) ->
            val h2Line = h2Elements
                .takeWhile { it != '\n' }
                .split(" {#")

            val h2Head = h2Line[0]

            if (h2Line.size == 2)
                anchorMapping[h2Line[1].takeWhile { it != '}' }] = index + 1

            val h2Body = h2Elements
                .dropWhile { it != '\n' }
                .split("\n##")
                .map { it.trim() }
                .map { rawBlocks ->
                    val h3Head = when {
                        rawBlocks.startsWith("# ") -> rawBlocks
                            .drop(2)
                            .takeWhile { it != '\n' }
                        else -> null
                    }

                    val blocks = rawBlocks
                        .drop(h3Head?.let { it.length + 2 } ?: 0)
                        .split("\n![](")
                        .flatMap { block -> when {
                            block.startsWith("http") -> {
                                val ref = block
                                    .takeWhile { it != ')' }
                                val text = block
                                    .drop(ref.length + 2)
                                    .trim()

                                when {
                                    text.isEmpty() -> listOf(Either.Right(URL(ref)))
                                    else -> listOf(Either.Right(URL(ref)), Either.Left(text))
                                }
                            }
                            else -> listOf(Either.Left(block))
                        } }

                    tuple(h3Head, blocks)
                }

            tuple(h2Head, h2Body)
        }

    return tuple(document, anchorMapping)
}
