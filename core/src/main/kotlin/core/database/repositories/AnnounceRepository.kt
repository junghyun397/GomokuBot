package core.database.repositories

import core.database.DatabaseConnection
import core.database.entities.Announce
import core.interact.i18n.Language
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import utils.lang.tuple
import java.time.LocalDateTime
import java.util.*

object AnnounceRepository {

    fun getLatestAnnounceId(connection: DatabaseConnection): Int? =
        when {
            connection.localCaches.announceCache.isNotEmpty() -> connection.localCaches.announceCache.lastKey()
            else -> null
        }

    fun getAnnouncesSince(connection: DatabaseConnection, announceIndex: Int): List<Map<Language, Announce>> =
        connection.localCaches.announceCache
            .tailMap(announceIndex)
            .values
            .toList()

    suspend fun fetchAnnounces(connection: DatabaseConnection): SortedMap<Int, Map<Language, Announce>> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("SELECT * FROM announce")
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    val date = row["create_date"] as LocalDateTime

                    val announces = Json.parseToJsonElement(row["contents"] as String)
                        .jsonObject
                        .entries
                        .associate { (languageCode, rawContent) ->
                            val language = Language.entries
                                .find { it.container.languageCode() == languageCode.uppercase() }
                                ?: Language.ENG

                            val content = Announce(
                                rawContent.jsonObject["title"]!!.jsonPrimitive.content,
                                rawContent.jsonObject["content"]!!.jsonPrimitive.content,
                                date
                            )

                            language to content
                        }

                    tuple(row["announce_id"] as Int, announces)
                }
            }
            .filter { (_, announces) -> announces.containsKey(Language.ENG) }
            .collectList()
            .map { it.toMap().toSortedMap() }
            .awaitSingle()

}
