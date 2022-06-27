package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.database.DatabaseManager
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred

class RankCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching {
        val rankings = DatabaseManager.fetchRankings(bot.databaseConnection)

        val io = producer.produceRankings(publisher, config.language.container, rankings)
            .map { it.launch(); Order.Unit }

        io to this.asCommandReport("succeed", user)
    }

}
