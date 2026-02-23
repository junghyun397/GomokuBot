package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.assets.aiUser
import core.database.repositories.UserProfileRepository
import core.database.repositories.UserStatsRepository
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.lang.tuple

sealed interface RankScope {

    object Global : RankScope { override fun toString() = "RankScope.Global" }

    data class Channel(val target: core.assets.Channel) : RankScope

    data class User(val target: core.assets.User) : RankScope

}

class RankCommand(private val scope: RankScope) : Command {

    override val name = "rank"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val rankings = when (this.scope) {
            is RankScope.Global -> UserStatsRepository.fetchRankings(bot.dbConnection)
                .map { tuple(UserProfileRepository.retrieveUser(bot.dbConnection, it.userId), it) }
            is RankScope.Channel -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
                .map { tuple(UserProfileRepository.retrieveUser(bot.dbConnection, it.userId), it) }
            is RankScope.User -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
                .map { stats ->
                    when (stats.userId) {
                        aiUser.id -> tuple(aiUser, stats)
                        else -> tuple(UserProfileRepository.retrieveUser(bot.dbConnection, stats.userId), stats)
                    }
                }
        }

        val io = effect {
            service.buildRankings(publishers.plain, config.language.container, rankings)
                .launch()()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("$scope scope", guild, user))
    }

}
