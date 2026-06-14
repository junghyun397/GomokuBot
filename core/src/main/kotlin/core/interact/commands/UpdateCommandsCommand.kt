package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.tuple

class UpdateCommandsCommand(
    command: Command,
    private val deprecates: List<String>,
    private val adds: List<String>
) : UnionCommand(command) {

    override val name = "update-commands"

    override suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet
    ) = runCatching {
        val io: Effect<Nothing, List<Order>> = effect { listOf(Order.UpsertCommands(config.language.container)) }

        val report = this.writeCommandReport("deprecates = $deprecates, adds = $adds", channel, user)

        tuple(io, report, channel, user)
    }

}
