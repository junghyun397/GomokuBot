package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.BoardStyle
import core.session.entities.ChannelConfig
import utils.tuple

class StyleCommand(private val style: BoardStyle) : Command {

    override val name = "style"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
    ) = runCatching {
        SessionManager.updateChannelConfig(bot.sessions, channel, config.copy(boardStyle = this.style))

        val io = effect {
            service.buildMessage(
                publishers.windowed,
                PlatformMessage(config.language.container.styleUpdated(this@StyleCommand.style.sample.styleName))
            )
                .launch()()
        }

        tuple(io, this.writeCommandReport("set style ${config.boardStyle.name} to ${this.style.name}", channel, user))
    }

}
