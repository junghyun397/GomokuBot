package core.session

import core.assets.ChannelUid
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.database.repositories.GameRecordRepository
import core.database.repositories.UserRatingRepository
import core.engine.EloRating
import core.session.entities.EngineGameSession
import core.session.entities.GameSession
import kotlin.time.Clock

object StatsManager {

    suspend fun uploadGameRecord(connection: DatabaseConnection, channelId: ChannelUid, session: GameSession) {
        val result = session.gameResult

        if (result == null || !session.recording || null in session.state.history)
            return

        val record = if (session is EngineGameSession) {
            val wld = when (result.winner) {
                session.userColor -> EloRating.MatchResult.WIN
                null -> EloRating.MatchResult.DRAW
                else -> EloRating.MatchResult.LOSE
            }

            val delta = session.userRating.delta(session.engineLevel.rating, wld)

            UserRatingRepository.upsertUserRating(connection, session.humanPlayer.id, session.userRating + delta)

            val record = GameRecord(
                gameRecordId = null,
                channelId = channelId,
                users = session.users,
                rule = session.rule,
                history = session.state.history,
                gameResult = result,
                engineLevel = session.engineLevel,
                ratingDelta = delta,
                date = Clock.System.now()
            )

            record
        } else {
            GameRecord(
                gameRecordId = null,
                channelId = channelId,
                users = session.users,
                rule = session.rule,
                history = session.state.history,
                gameResult = result,
                engineLevel = null,
                ratingDelta = null,
                date = Clock.System.now()
            )
        }

        GameRecordRepository.uploadGameRecord(connection, record)
    }

}
