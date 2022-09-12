package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
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

    object Guild : RankScope { override fun toString() = "RankScope.Guild" }

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
            is RankScope.Guild -> UserStatsRepository.fetchRankings(bot.dbConnection, guild.id)
            is RankScope.User -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
        }

        val tuple = rankings
            .map { UserProfileRepository.retrieveUser(bot.dbConnection, it.userId) and it }

        val io = producer.produceRankings(publishers.plain, config.language.container, tuple)
            .launch()
            .map { emptyList<Order>() }

        io and this.asCommandReport("$scope scope", guild, user)
    }

}
