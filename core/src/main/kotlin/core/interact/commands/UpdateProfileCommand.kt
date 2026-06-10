package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.ChannelProfileRepository
import core.database.repositories.UserProfileRepository
import core.interact.Order
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.lang.tuple

class UpdateProfileCommand(
    command: Command,
    private val newUser: User.Human?,
    private val newChannel: Channel?,
) : UnionCommand(command) {

    override val name = "update-profile"

    override suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        this.newUser?.let {
            UserProfileRepository.upsertUser(bot.dbConnection, it)
        }

        val thenUser = this.newUser ?: user

        this.newChannel?.let {
            ChannelProfileRepository.upsertChannel(bot.dbConnection, it)
        }

        val thenChannel = this.newChannel ?: channel

        val io: Effect<Nothing, List<Order>> = effect { emptyOrders }

        val report = this.writeCommandReport("succeed", channel, user)

        tuple(io, report, thenChannel, thenUser)
    }

}
