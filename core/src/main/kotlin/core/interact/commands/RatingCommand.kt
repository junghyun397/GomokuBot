package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.GameRecordRepository
import core.database.repositories.UserRatingRepository
import core.engine.MintakaServer
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class RatingCommand(
    private val target: User.Human? = null,
) : Command {

    override val name = "rating"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val target = this.target ?: user

        val rating = UserRatingRepository.retrieveUserRating(target.id)
        val recentDelta = GameRecordRepository.retrieveRecentDelta(target)

        val io: Effect<Nothing, Unit> = effect {
            publishers.plain(AppMessage.Rating(target, rating, recentDelta))
                .launch().bind()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "target $target", channel, user))
    }

}
