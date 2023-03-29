package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.interact.Order
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import utils.lang.shift
import utils.lang.tuple
import utils.structs.IO
import utils.structs.flatMap
import utils.structs.map

class ExpireGameCommand(
    private val guildSession: GuildSession,
    private val session: GameSession,
    private val channelAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-game"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        service: MessagingService<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        val (finishedSession, result) = GameManager.resignSession(session, GameResult.Cause.TIMEOUT, session.player)

        GameManager.finishSession(bot, guildSession.guild, finishedSession, result)

        val io = if (this.channelAvailable) {
            val message = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val noticePublisher = publisher.plain

            val boardPublisher = noticePublisher
                .shift(guildSession.config.swapType == SwapType.EDIT && message != null) { publisher.edit(message!!) }

            val finishIO = buildFinishProcedure(bot,
                service, boardPublisher, guildSession.config, session, finishedSession)
                .map { it + Order.ArchiveSession(finishedSession, guildSession.config.archivePolicy) }

            val noticeIO = when (session) {
                is PvpGameSession, is OpeningSession -> service
                    .buildTimeoutPVP(noticePublisher, guildSession.config.language.container, session.player, session.nextPlayer)
                is AiGameSession -> service
                    .buildTimeoutPVE(noticePublisher, guildSession.config.language.container, session.owner)
            }
                .launch()

            noticeIO.flatMap { finishIO }
        } else IO.value(emptyOrders)

        val report = this.writeCommandReport("expired, terminate session by $result", guild)

        tuple(io, report)
    }

}
