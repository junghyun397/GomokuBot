package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.session.entities.GuildConfig

interface InternalCommand {

    val name: String

    suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        service: MessagingService<A, B>,
        publisher: PublisherSet<A, B>
    ): CommandResult

}
