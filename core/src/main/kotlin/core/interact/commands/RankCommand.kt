package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.database.repositories.UserProfileRepository
import core.database.repositories.UserStatsRepository
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.lang.and
import utils.structs.flatMap
import utils.structs.map

sealed class RankScope {

    object Global : RankScope()

    object Guild : RankScope()

    class User(val target: core.assets.User) : RankScope()

}

class RankCommand(
    override val name: String,
    private val scope: RankScope,
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        val rankings = when (this.scope) {
            is RankScope.Global -> UserStatsRepository.fetchRankings(bot.dbConnection)
            is RankScope.Guild -> UserStatsRepository.fetchRankings(bot.dbConnection, guild.id)
            is RankScope.User -> UserStatsRepository.fetchRankings(bot.dbConnection, scope.target.id)
        }

        val tuple = rankings
            .map { UserProfileRepository.retrieveUser(bot.dbConnection, it.userId) and it }

        val io = producer.produceRankings(publisher, config.language.container, tuple)
            .flatMap { it.launch() }
            .map { emptyList<Order>() }

        io and this.asCommandReport("succeed", user)
    }

}
