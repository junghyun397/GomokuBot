package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.assets.aiUser
import core.database.repositories.UserProfileRepository
import core.database.repositories.UserStatsRepository
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.lang.and
import utils.structs.map

sealed interface RankScope {

    object Global : RankScope { override fun toString() = "RankScope.Global" }

    data class Guild(val target: core.assets.Guild) : RankScope

    data class User(val target: core.assets.User) : RankScope

}

class RankCommand(private val scope: RankScope) : Command {

    override val name = "rank"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val rankings = when (this.scope) {
            is RankScope.Global -> UserStatsRepository.fetchRankings(bot.dbConnection)
                .map { UserProfileRepository.retrieveUser(bot.dbConnection, it.userId) and it }
            is RankScope.Guild -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
                .map { UserProfileRepository.retrieveUser(bot.dbConnection, it.userId) and it }
            is RankScope.User -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
                .map { stats ->
                    when (stats.userId) {
                        aiUser.id -> aiUser and stats
                        else -> UserProfileRepository.retrieveUser(bot.dbConnection, stats.userId) and stats
                    }
                }
        }

        val io = producer.produceRankings(publishers.plain, config.language.container, rankings)
            .launch()
            .map { emptyList<Order>() }

        io and this.asCommandReport("$scope scope", guild, user)
    }

}
