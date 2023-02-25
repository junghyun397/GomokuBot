package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class HelpCommand(private val sendSettings: Boolean) : Command {

    override val name = "help"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val io = when (this.sendSettings) {
            true -> buildCombinedHelpProcedure(bot, config, publishers.plain, producer, 0)
            else -> buildHelpProcedure(bot, config, publishers.plain, producer)
        }
            .map { emptyOrders }

        tuple(io, this.asCommandReport("succeed", guild, user))
    }

}
