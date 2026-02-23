package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.BoardStyle
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.tuple

class StyleCommand(private val style: BoardStyle) : Command {

    override val name = "style"

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
        SessionManager.updateGuildConfig(bot.sessions, guild, config.copy(boardStyle = style))

        val io = effect {
            service.buildStyleUpdated(publishers.windowed, config.language.container, style.sample.styleName)
                .launch()()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("set style ${config.boardStyle.name} to ${style.name}", guild, user))
    }

}
