package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.OpeningSession
import core.session.entities.RequestSession
import utils.lang.tuple
import utils.structs.flatMap
import utils.structs.map

class AcceptCommand(
    private val requestSession: RequestSession,
) : Command {

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

        SessionManager.removeRequestSession(bot.sessions, guild, this.requestSession.owner.id)
        SessionManager.putGameSession(bot.sessions, guild, gameSession)

        val guidePublisher = SessionManager.checkoutMessages(bot.sessions, requestSession.messageBufferKey)
            ?.let { publishers.edit(it.first()) }
            ?: publishers.plain

        val beginIO = when (gameSession) {
            is OpeningSession ->
                service.buildBeginsOpening(guidePublisher, config.language.container, gameSession.nextPlayer, gameSession.player, gameSession.ruleKind)
            else ->
                service.buildBeginsPVP(guidePublisher, config.language.container, gameSession.player, gameSession.nextPlayer)
        }
            .launch()

        val boardIO = buildBoardProcedure(bot, guild, config, service, publishers.plain, gameSession)

        val io = beginIO
            .flatMap { boardIO }
            .map { emptyOrders }

        tuple(io, this.writeCommandReport("accept ${requestSession.owner}'s request", guild, user))
    }

}
