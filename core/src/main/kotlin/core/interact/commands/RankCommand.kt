package core.interact.commands

import core.BotContext
import core.interact.Order
import core.assets.User
import core.database.DatabaseManager
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.structs.IO

class RankCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        val rankings = DatabaseManager.retrieveRanking(context.databaseConnection)

        val io = producer.produceRankings(publisher, config.language.container, rankings).map { it.launch(); Order.Unit }

        io to this.asCommandReport("succeed", user)
    }

}