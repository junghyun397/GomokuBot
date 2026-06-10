package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.lang.tuple

class HelpCommand(
    private val sendSettings: Boolean,
    private val page: Int,
) : Command {

    override val name = "help"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet,
    ) = runCatching {
        val io = effect {
            when (this@HelpCommand.sendSettings) {
                true -> buildCombinedHelpProcedure(bot, config, publishers.plain, service, page)
                else -> buildHelpProcedure(bot, config, publishers.plain, service, page)
            }()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("sent", channel, user))
    }

}
