package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple

class UpdateCommandsCommand(
    command: Command,
    private val deprecates: List<String>,
    private val adds: List<String>
) : UnionCommand(command) {

    override val name = "update-commands"

    override suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val io: Effect<Nothing, List<Order>> = effect { listOf(Order.UpsertCommands(config.language.container)) }

        val report = this.writeCommandReport("deprecates = $deprecates, adds = $adds", guild, user)

        tuple(io, report, guild, user)
    }

}
