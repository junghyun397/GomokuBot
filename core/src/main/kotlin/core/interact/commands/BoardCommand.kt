package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class BoardCommand(
    private val session: GameSession
) : Command {

    override val name = "board"

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
        val io = buildBoardProcedure(bot, guild, config, service, publishers.plain, session)
            .map { emptyOrders }

        tuple(io, this.writeCommandReport("reopen board", guild, user))
    }

}
