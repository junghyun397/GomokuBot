package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.entities.GameRecord
import core.interact.Order
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.tuple

class ReplayCommand(
    private val record: GameRecord,
    private val messageRef: MessageRef,
) : Command {

    override val name = "replay"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        val io: Effect<Nothing, List<Order>> = effect {
            service.buildReplay(publishers.edit(messageRef), config.language.container, this@ReplayCommand.record)
                .launch()()

            emptyOrders
        }

        tuple(io, this.writeCommandReport("view record", channel, user))
    }

}
