package utils.assets

import utils.lang.and
import utils.structs.Either

typealias MarkdownLikeDocument = List<Pair<String, List<Pair<String?, List<Either<String, URL>>>>>>

// ## h2Title
// ### h3Title
// ![](https://URL(ref))
// text
fun parseMarkdownLikeDocument(source: String): MarkdownLikeDocument =
    source.split("\n## ")
        .map { if (it.startsWith("## ")) it.drop(3) else it }
        .map { it.trim() }
        .map { h2Elements ->
            val h2Head = h2Elements
                .takeWhile { it != '\n' }

            val h2Body = h2Elements
                .drop(h2Head.length)
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

                    h3Head and blocks
                }

            h2Head and h2Body
        }
