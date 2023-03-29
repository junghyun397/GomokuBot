package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.RequestSession
import core.session.entities.TaraguchiOpeningSession
import utils.lang.tuple
import utils.structs.flatMap
import utils.structs.map

class AcceptCommand(private val requestSession: RequestSession) : Command {

    override val name = "accept"

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
        val gameSession = GameManager.generatePvpSession(bot, this.requestSession.owner, this.requestSession.opponent, this.requestSession.rule)

        SessionManager.putGameSession(bot.sessions, guild, gameSession)
        SessionManager.removeRequestSession(bot.sessions, guild, this.requestSession.owner.id)

        val beginIO = when (gameSession) {
            is TaraguchiOpeningSession ->
                service.buildBeginsOpening(publishers.plain, config.language.container, gameSession.owner, gameSession.opponent, gameSession.ownerHasBlack)
            else ->
                service.buildBeginsPVP(publishers.plain, config.language.container, gameSession.player, gameSession.nextPlayer)
        }
            .launch()

        val boardIO = buildBoardProcedure(bot, guild, config, service, publishers.plain, gameSession)

        val io = beginIO
            .flatMap { boardIO }
            .map { listOf(Order.DeleteSource) }

        tuple(io, this.writeCommandReport("accept ${requestSession.owner}'s request", guild, user))
    }

}
