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
import utils.structs.map

class RankCommand(override val name: String) : Command {

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
        val rankings = UserStatsRepository.fetchRankings(bot.dbConnection)

        val combined = rankings
            .map { UserProfileRepository.retrieveUser(bot.dbConnection, it.userId) to it }

        val io = producer.produceRankings(publisher, config.language.container, combined)
            .map { it.launch(); emptyList<Order>() }

        io to this.asCommandReport("succeed", user)
    }

}
