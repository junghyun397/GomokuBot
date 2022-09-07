package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import utils.lang.and
import utils.structs.flatMap
import utils.structs.map

class AcceptCommand(private val requestSession: RequestSession) : Command {

    override val name = "accept"

    override val responseFlag = ResponseFlag.IMMEDIATELY

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val gameSession = GameManager.generatePvpSession(
            bot.config.gameExpireOffset,
            this.requestSession.owner,
            this.requestSession.opponent,
        )

        SessionManager.putGameSession(bot.sessions, guild, gameSession)
        SessionManager.removeRequestSession(bot.sessions, guild, this.requestSession.owner.id)

        val beginIO = producer.produceBeginsPVP(publishers.plain, config.language.container, gameSession.player, gameSession.nextPlayer)
            .launch()

        val boardIO = buildBoardProcedure(bot, guild, config, producer, publishers.plain, gameSession)

        val io = beginIO
            .flatMap { boardIO }
            .map { listOf(Order.DeleteSource) }

        io and this.asCommandReport("accept ${requestSession.owner}'s request", guild, user)
    }

}
