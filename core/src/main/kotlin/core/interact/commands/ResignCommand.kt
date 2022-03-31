package core.interact.commands

import core.BotContext
import core.assets.Order
import core.assets.User
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.monads.IO

class ResignCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching {
        val currentSession = SessionManager.retrieveGameSession(context.sessionRepository, config.id, user.id)
            ?: return@runCatching run {
                IO { Order.UNIT } to this.asCommandReport("$user surrendered but session not found")
            }

        SessionManager.removeGameSession(context.sessionRepository, config.id, currentSession.owner.id)
        IO { Order.UNIT } to this.asCommandReport("$currentSession surrendered")
    }

}