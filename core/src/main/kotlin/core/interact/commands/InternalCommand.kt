package core.interact.commands

import core.BotContext
import core.assets.Channel
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.session.entities.ChannelConfig

interface InternalCommand {

    val name: String

    suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        service: MessagingService<A, B>,
        publisher: PublisherSet<A, B>
    ): CommandResult

}
