package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.IO

class GuildLeaveCommand : InternalCommand {

    override val name = "guild-leave"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        producer: MessageProducer<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        tuple(IO.value(emptyOrders), this.writeCommandReport("goodbye", guild))
    }

}
