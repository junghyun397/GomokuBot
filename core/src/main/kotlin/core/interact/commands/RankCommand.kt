package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.UserProfileRepository
import core.database.repositories.UserStatsRepository
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.entities.ChannelConfig
import utils.tuple
import kotlin.time.Instant

sealed interface RankScope {

    object Global : RankScope { override fun toString() = "RankScope.Global" }

    data class Channel(val target: core.assets.Channel) : RankScope

    data class User(val target: core.assets.User.Human) : RankScope

}

class RankCommand(private val scope: RankScope) : Command {

    override val name = "rank"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val rankings = when (this.scope) {
            is RankScope.Global -> UserStatsRepository.fetchRankings(bot.dbConnection)
                .map { tuple(UserProfileRepository.retrieveUser(bot.dbConnection, it.userId), it) }
            is RankScope.Channel -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
                .map { tuple(UserProfileRepository.retrieveUser(bot.dbConnection, it.userId), it) }
            is RankScope.User -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
                .map { (userUid, stats) ->
                    tuple(
                        userUid?.let { UserProfileRepository.retrieveUser(bot.dbConnection, it) } ?: User.GomokuBot,
                        stats
                    )
                }
        }

        val io = effect {
            service.buildRankings(publishers.plain, config.language.container, rankings)
                .launch()()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "$scope scope", channel, user))
    }

}
