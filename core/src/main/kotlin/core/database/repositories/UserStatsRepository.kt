package core.database.repositories

import core.assets.ChannelUid
import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.entities.UserStats
import core.database.jooq.tables.records.UserStatsRecord
import core.database.jooq.tables.references.GAME_RECORD
import core.database.jooq.tables.references.USER_STATS
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jooq.impl.DSL
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import renju.notation.Color
import renju.notation.GameResult
import utils.toUtcInstant
import utils.tuple
import utils.unreachable
import kotlin.time.Clock

object UserStatsRepository {

    suspend fun fetchUserStats(connection: DatabaseConnection, userUid: UserUid): UserStats =
        Mono.from(
            connection.jooq
                .selectFrom(USER_STATS)
                .where(USER_STATS.USER_ID.eq(userUid.uuid))
        )
            .map { this.extractUserStats(it) }
            .awaitSingleOrNull()
            ?: UserStats(userUid)

    suspend fun fetchRankings(connection: DatabaseConnection): List<UserStats> =
        Flux.from(
            connection.jooq
                .selectFrom(USER_STATS)
                .orderBy(
                    DSL.field("{0} + {1}", Int::class.java, USER_STATS.BLACK_WINS, USER_STATS.WHITE_WINS).desc()
                )
                .limit(10)
        )
            .map { this.extractUserStats(it) }
            .collectList()
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection, channelUid: ChannelUid): List<UserStats> =
        Flux.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(GAME_RECORD.CHANNEL_ID.eq(channelUid.uuid))
                .and(GAME_RECORD.ENGINE_LEVEL.isNotNull())
        )
            .map { record ->
                val maybeBlackId = record.blackId
                val maybeWhiteId = record.whiteId

                val recordResult = GameResult.fromId(record.cause!!, Color.from(record.winColor?.toByte()))!!

                when {
                    maybeBlackId != null -> tuple(UserUid(maybeBlackId), Color.BLACK, recordResult)
                    maybeWhiteId != null -> tuple(UserUid(maybeWhiteId), Color.WHITE, recordResult)
                    else -> unreachable()
                }
            }
            .collectList()
            .map { records -> this.buildUserStats(records).sortedDescending() }
            .awaitSingle()

    suspend fun fetchRankings(connection: DatabaseConnection, userUid: UserUid): List<Pair<UserUid?, UserStats>> =
        Flux.from(
            connection.jooq
                .selectFrom(GAME_RECORD)
                .where(
                    GAME_RECORD.BLACK_ID.eq(userUid.uuid)
                        .and(GAME_RECORD.WHITE_ID.isNotNull())
                        .or(GAME_RECORD.WHITE_ID.eq(userUid.uuid).and(GAME_RECORD.BLACK_ID.isNotNull()))
                )
                .and(GAME_RECORD.ENGINE_LEVEL.isNull())
        )
            .map { record ->
                val blackId = UserUid(record.blackId!!)
                val whiteId = UserUid(record.whiteId!!)

                val recordResult = GameResult.fromId(record.cause!!, Color.from(record.winColor?.toByte()))!!

                when {
                    blackId != userUid -> tuple(blackId, Color.BLACK, recordResult)
                    whiteId != userUid -> tuple(whiteId, Color.WHITE, recordResult)
                    else -> unreachable()
                }
            }
            .collectList()
            .awaitSingle()
            .let { records ->
                val userStats = this.buildUserStats(records).map { it.userId to it }

                val aiStats = this.fetchUserStats(connection, userUid).reversed()

                val unionRanking = when (aiStats.isEmpty) {
                    true -> userStats
                    else -> userStats + (null to aiStats)
                }

                unionRanking.sortedByDescending { it.second }
            }

    private fun extractUserStats(record: UserStatsRecord): UserStats =
        UserStats(
            userId = UserUid(record.userId!!),
            blackWins = record.blackWins!!,
            blackLosses = record.blackLosses!!,
            blackDraws = record.blackDraws!!,

            whiteWins = record.whiteWins!!,
            whiteLosses = record.whiteLosses!!,
            whiteDraws = record.whiteDraws!!,

            lastUpdate = record.lastUpdate!!.toUtcInstant()
        )

    private fun buildUserStats(records: List<Triple<UserUid, Color, GameResult>>): List<UserStats> =
        records
            .groupBy { (id, _, _) -> id }
            .map { (id, tuples) ->
                val blackTotal = tuples.count { (_, color, _) -> color == Color.BLACK }
                val whiteTotal = tuples.size - blackTotal

                val blackWins = tuples.count { (_, color, result) -> color == Color.BLACK && result.winner == Color.BLACK }
                val whiteWins = tuples.count { (_, color, result) -> color == Color.WHITE && result.winner == Color.WHITE }

                val blackLosses = tuples.count { (_, color, result) -> color == Color.BLACK && result.winner == Color.WHITE }
                val whiteLosses = tuples.count { (_, color, result) -> color == Color.WHITE && result.winner == Color.BLACK }

                UserStats(
                    userId = id,
                    blackWins = blackWins,
                    blackLosses = blackLosses,
                    blackDraws = blackTotal - blackWins - blackLosses,
                    whiteWins = whiteWins,
                    whiteLosses = whiteLosses,
                    whiteDraws = whiteTotal - whiteWins - whiteLosses,
                    lastUpdate = Clock.System.now()
                )
            }

}
