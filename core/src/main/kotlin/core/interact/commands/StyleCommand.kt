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
import core.session.BoardStyle
import core.session.SessionManager
import core.session.entities.ChannelConfig
import utils.lang.tuple

class StyleCommand(private val style: BoardStyle) : Command {

    override val name = "style"

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
        SessionManager.updateChannelConfig(bot.sessions, channel, config.copy(boardStyle = style))

        val io = effect {
            service.buildStyleUpdated(publishers.windowed, config.language.container, style.sample.styleName)
                .launch()()
            emptyOrders
        }

        tuple(io, this.writeCommandReport("set style ${config.boardStyle.name} to ${style.name}", channel, user))
    }

}
