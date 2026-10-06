package core.session

import core.assets.ChannelUid
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.database.repositories.GameRecordRepository
import core.database.repositories.UserRatingRepository
import core.session.entities.EngineGameSession
import core.session.entities.GameSession
import kotlin.time.Clock

object StatsManager {

    context(connection: DatabaseConnection)
    suspend fun uploadGameRecord(channelId: ChannelUid, session: GameSession) {
        val result = session.gameResult

        if (result == null || !session.recording || null in session.state.history)
            return

        val record = if (session is EngineGameSession) {
            val ratingDelta = session.ratingDelta!!

            UserRatingRepository.upsertUserRating(session.humanPlayer.id, session.userRating + ratingDelta)

            GameRecord(
                gameRecordId = null,
                channelId = channelId,
                users = session.users,
                rule = session.rule,
                history = session.state.history,
                gameResult = result,
                engineLevel = session.engineLevel,
                ratingDelta = ratingDelta.delta,
                date = Clock.System.now()
            )
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

        GameRecordRepository.uploadGameRecord(record)
    }

}
