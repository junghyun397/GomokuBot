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
import utils.lang.pair
import utils.structs.map

class UpdateCommandsCommand(
    private val command: Command,
    private val deprecates: List<String>,
    private val adds: List<String>
) : Command {

    override val name = "update-commands"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        this.command
            .execute(bot, config, guild, user, producer, messageRef, publishers)
            .map { (originalIO, originalReport) ->
                val io = originalIO
                    .map { originalOrder -> originalOrder + Order.UpsertCommands(config.language.container) }

                io pair originalReport + this.asCommandReport("deprecates = $deprecates, adds = $adds", guild, user)
            }
            .getOrThrow()
    }

}
