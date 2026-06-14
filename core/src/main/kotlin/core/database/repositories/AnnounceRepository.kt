package core.database.repositories

import core.database.DatabaseConnection
import core.database.entities.Announce
import core.database.jooq.tables.records.AnnounceRecord
import core.database.jooq.tables.references.ANNOUNCE
import core.interact.i18n.Language
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import reactor.core.publisher.Flux
import utils.tuple
import java.util.*

object AnnounceRepository {

    fun getLatestAnnounceId(connection: DatabaseConnection): Int? =
        when {
            connection.localCaches.announceCache.isNotEmpty() -> connection.localCaches.announceCache.lastKey()
            else -> null
        }

    fun getAnnouncesSince(connection: DatabaseConnection, announceIndex: Int): List<Map<Language, Announce>> =
        connection.localCaches.announceCache
            .tailMap(announceIndex + 1)
            .values
            .toList()

    fun getLatestAnnounce(connection: DatabaseConnection): Map<Language, Announce> =
        connection.localCaches.announceCache[this.getLatestAnnounceId(connection)]!!

    suspend fun fetchAnnounces(connection: DatabaseConnection): SortedMap<Int, Map<Language, Announce>> =
        Flux.from(
            connection.jooq
                .selectFrom(ANNOUNCE)
        )
            .map { this.extractAnnounces(it) }
            .filter { (_, announces) -> announces.containsKey(Language.ENG) }
            .collectList()
            .map { it.toMap().toSortedMap() }
            .awaitSingle()

    private fun extractAnnounces(record: AnnounceRecord): Pair<Int, Map<Language, Announce>> {
        val date = record.createDate!!

        val announces = Json.parseToJsonElement(record.contents!!)
            .jsonObject
            .entries
            .associate { (languageCode, rawContent) ->
                val language = Language.entries
                    .find { it.container.languageCode() == languageCode.uppercase() }
                    ?: Language.ENG

                val announce = Announce(
                    rawContent.jsonObject["title"]!!.jsonPrimitive.content,
                    rawContent.jsonObject["content"]!!.jsonPrimitive.content,
                    date
                )

                language to announce
            }

        return tuple(record.announceId!!, announces)
    }

}
