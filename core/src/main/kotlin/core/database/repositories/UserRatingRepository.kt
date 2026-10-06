package core.database.repositories

import core.assets.UserUid
import core.database.DatabaseConnection
import core.database.jooq.tables.records.UserRatingRecord
import core.database.jooq.tables.references.USER_RATING
import core.engine.EloRating
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Mono

object UserRatingRepository {

    context(connection: DatabaseConnection)
    suspend fun retrieveUserRating(userUid: UserUid): EloRating {
        connection.localCaches.userRatingCache
            .getIfPresent(userUid)
            ?.let { return it }

        val rating = Mono.from(
            connection.jooq
                .selectFrom(USER_RATING)
                .where(USER_RATING.USER_ID.eq(userUid.uuid))
        )
            .map { this.extractUserRating(it) }
            .awaitSingleOrNull()
            ?: EloRating.STARTING_RATING

        this.cacheUserRating(userUid, rating)

        return rating
    }

    context(connection: DatabaseConnection)
    suspend fun upsertUserRating(userUid: UserUid, rating: EloRating) {
        this.cacheUserRating(userUid, rating)

        Mono.from(
            connection.jooq
                .insertInto(USER_RATING)
                .set(USER_RATING.USER_ID, userUid.uuid)
                .set(USER_RATING.RATING, rating.rating.toDouble())
                .onConflict(USER_RATING.USER_ID)
                .doUpdate()
                .set(USER_RATING.RATING, rating.rating.toDouble())
        )
            .awaitSingle()
    }

    private fun extractUserRating(record: UserRatingRecord): EloRating =
        EloRating(record.rating!!.toFloat())

    context(connection: DatabaseConnection)
    private fun cacheUserRating(userUid: UserUid, rating: EloRating) {
        connection.localCaches.userRatingCache.put(userUid, rating)
    }

}
