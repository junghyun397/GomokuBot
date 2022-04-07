package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.message.graphics.BoardStyle
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig

class StyleCommand(override val command: String, private val style: BoardStyle) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(context.sessionRepository, config.id, config.copy(boardStyle = style))

        val io = producer.produceStyleUpdated(publisher, config.language.container, style.sample.styleName)
            .map { it.launch(); Order.Unit }

        io to this.asCommandReport("${config.boardStyle.name} to ${style.name}", user)
    }

}
