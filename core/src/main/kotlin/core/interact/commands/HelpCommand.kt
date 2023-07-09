package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class HelpCommand(
    private val sendSettings: Boolean,
    private val page: Int,
) : Command {

    override val name = "help"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val io = when (this.sendSettings) {
            true -> buildCombinedHelpProcedure(bot, config, publishers.plain, service, page)
            else -> buildHelpProcedure(bot, config, publishers.plain, service, page)
        }
            .map { emptyOrders }

        tuple(io, this.writeCommandReport("sent", guild, user))
    }

}
