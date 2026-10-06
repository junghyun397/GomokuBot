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
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Table
import org.jooq.impl.DSL
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import utils.toUtcInstant
import java.util.*

object UserStatsRepository {

    context(connection: DatabaseConnection)
    suspend fun fetchUserStats(userUid: UserUid): UserStats =
        Mono.from(
            connection.jooq
                .selectFrom(USER_STATS)
                .where(USER_STATS.USER_ID.eq(userUid.uuid))
        )
            .map { this.extractUserStats(it) }
            .awaitSingleOrNull()
            ?: UserStats(userUid)

    context(connection: DatabaseConnection)
    suspend fun fetchRankings(): List<UserStats> =
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

    context(connection: DatabaseConnection)
    suspend fun fetchRankings(channelUid: ChannelUid): List<UserStats> {
        val rankings = this.aggregateStats(
            userId = DSL.coalesce(GAME_RECORD.BLACK_ID, GAME_RECORD.WHITE_ID),
            isBlack = GAME_RECORD.BLACK_ID.isNotNull,
            condition = GAME_RECORD.CHANNEL_ID.eq(channelUid.uuid).and(GAME_RECORD.ENGINE_LEVEL.isNotNull),
        ).asTable("rankings")

        return Flux.from(connection.jooq.selectFrom(rankings).orderBy(this.rankingOrder(rankings)))
            .map { this.extractUserStats(it.into(USER_STATS)) }
            .collectList()
            .awaitSingle()
    }

    context(connection: DatabaseConnection)
    suspend fun fetchRankings(userUid: UserUid): List<Pair<UserUid?, UserStats>> {
        val isBlack = GAME_RECORD.WHITE_ID.eq(userUid.uuid)
        val opponents = this.aggregateStats(
            userId = DSL.`when`(isBlack, GAME_RECORD.BLACK_ID).otherwise(GAME_RECORD.WHITE_ID),
            isBlack = isBlack,
            condition = GAME_RECORD.BLACK_ID.eq(userUid.uuid).or(GAME_RECORD.WHITE_ID.eq(userUid.uuid))
                .and(GAME_RECORD.ENGINE_LEVEL.isNull),
        )

        // The AI's colors and results are the reverse of the user's lifetime statistics.
        val engine = connection.jooq
            .select(listOf(
                DSL.inline(null, USER_STATS.USER_ID.dataType).`as`(USER_STATS.USER_ID),
                USER_STATS.WHITE_LOSSES.`as`(USER_STATS.BLACK_WINS),
                USER_STATS.WHITE_WINS.`as`(USER_STATS.BLACK_LOSSES),
                USER_STATS.WHITE_DRAWS.`as`(USER_STATS.BLACK_DRAWS),
                USER_STATS.BLACK_LOSSES.`as`(USER_STATS.WHITE_WINS),
                USER_STATS.BLACK_WINS.`as`(USER_STATS.WHITE_LOSSES),
                USER_STATS.BLACK_DRAWS.`as`(USER_STATS.WHITE_DRAWS),
                USER_STATS.LAST_UPDATE,
            ))
            .from(USER_STATS)
            .where(USER_STATS.USER_ID.eq(userUid.uuid))
            .and(
                USER_STATS.BLACK_WINS.add(USER_STATS.BLACK_LOSSES).add(USER_STATS.BLACK_DRAWS)
                    .add(USER_STATS.WHITE_WINS).add(USER_STATS.WHITE_LOSSES).add(USER_STATS.WHITE_DRAWS).gt(0)
            )

        val rankings = opponents.unionAll(engine).asTable("rankings")

        return Flux.from(connection.jooq.selectFrom(rankings).orderBy(this.rankingOrder(rankings)))
            .map {
                val record = it.into(USER_STATS)
                val opponentId = record.userId?.let { id -> UserUid(id) }

                opponentId to this.extractUserStats(record, opponentId ?: userUid)
            }
            .collectList()
            .awaitSingle()
    }

    context(connection: DatabaseConnection)
    private fun aggregateStats(userId: Field<UUID?>, isBlack: Condition, condition: Condition) =
        connection.jooq
            .select(listOf(
                userId.`as`(USER_STATS.USER_ID),
                DSL.count().filterWhere(isBlack.and(GAME_RECORD.WIN_COLOR.eq(0))).`as`(USER_STATS.BLACK_WINS),
                DSL.count().filterWhere(isBlack.and(GAME_RECORD.WIN_COLOR.eq(1))).`as`(USER_STATS.BLACK_LOSSES),
                DSL.count().filterWhere(isBlack.and(this.drawCondition)).`as`(USER_STATS.BLACK_DRAWS),
                DSL.count().filterWhere(isBlack.not().and(GAME_RECORD.WIN_COLOR.eq(1))).`as`(USER_STATS.WHITE_WINS),
                DSL.count().filterWhere(isBlack.not().and(GAME_RECORD.WIN_COLOR.eq(0))).`as`(USER_STATS.WHITE_LOSSES),
                DSL.count().filterWhere(isBlack.not().and(this.drawCondition)).`as`(USER_STATS.WHITE_DRAWS),
                DSL.max(GAME_RECORD.CREATE_DATE).`as`(USER_STATS.LAST_UPDATE),
            ))
            .from(GAME_RECORD)
            .where(condition)
            .groupBy(DSL.field(USER_STATS.USER_ID.unqualifiedName))

    private val drawCondition = GAME_RECORD.WIN_COLOR.isDistinctFrom(0).and(GAME_RECORD.WIN_COLOR.isDistinctFrom(1))

    private fun rankingOrder(rankings: Table<*>) = listOf(
        rankings.field(USER_STATS.BLACK_WINS)!!.add(rankings.field(USER_STATS.WHITE_WINS)!!).desc(),
        rankings.field(USER_STATS.BLACK_LOSSES)!!.add(rankings.field(USER_STATS.WHITE_LOSSES)!!).asc(),
        rankings.field(USER_STATS.BLACK_DRAWS)!!.add(rankings.field(USER_STATS.WHITE_DRAWS)!!).desc(),
    )

    private fun extractUserStats(record: UserStatsRecord, userId: UserUid = UserUid(record.userId!!)): UserStats =
        UserStats(
            userId = userId,
            blackWins = record.blackWins!!,
            blackLosses = record.blackLosses!!,
            blackDraws = record.blackDraws!!,

            whiteWins = record.whiteWins!!,
            whiteLosses = record.whiteLosses!!,
            whiteDraws = record.whiteDraws!!,

            lastUpdate = record.lastUpdate!!.toUtcInstant()
        )

}
