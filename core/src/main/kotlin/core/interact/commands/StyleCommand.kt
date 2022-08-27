package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.BoardStyle
import core.session.SessionManager
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.lang.and
import utils.structs.flatMap
import utils.structs.map

class StyleCommand(override val name: String, private val style: BoardStyle) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>,
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, config.copy(boardStyle = style))

        val io = producer.produceStyleUpdated(publisher, config.language.container, style.sample.styleName)
            .flatMap { it.launch() }
            .map { emptyList<Order>() }

        io and this.asCommandReport("${config.boardStyle.name} to ${style.name}", user)
    }

}
