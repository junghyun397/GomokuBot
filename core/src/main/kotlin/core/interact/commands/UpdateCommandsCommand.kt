package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.IO

class UpdateCommandsCommand(
    command: Command,
    private val deprecates: List<String>,
    private val adds: List<String>
) : UnionCommand(command) {

    override val name = "update-commands"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val io = IO.unit { listOf(Order.UpsertCommands(config.language.container)) }

        val report = this.asCommandReport("deprecates = $deprecates, adds = $adds", guild, user)

        tuple(io, report, guild, user)
    }

}
