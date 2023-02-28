package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.BoardStyle
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class StyleCommand(private val style: BoardStyle) : Command {

    override val name = "style"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, config.copy(boardStyle = style))

        val io = producer.produceStyleUpdated(publishers.windowed, config.language.container, style.sample.styleName)
            .launch()
            .map { emptyOrders }

        tuple(io, this.writeCommandReport("set style ${config.boardStyle.name} to ${style.name}", guild, user))
    }

}
