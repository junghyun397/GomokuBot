package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.ResultDraw
import core.interact.reports.writeActionLog
import core.session.*
import core.session.entities.*
import utils.replaceIf
import utils.tuple
import kotlin.time.Instant

class ExpireGameCommand(
    private val session: GameSession,
) : InternalCommand {

    override val name = "expire-game"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        service: PlatformService,
        publisher: PublisherSet?,
        emittedTime: Instant,
    ) = runCatching {
        val session = when (this.session) {
            is PvpGameSession -> PvpGameManager.resign(this.session, null)
            is EngineGameSession -> EngineGameManager.resign(this.session, EngineGameManager.ResignCause.TIMEOUT)
            is OpeningSession -> PvpGameManager.resign(this.session, null)
        }

        SessionManager.deleteGameSession(bot.sessions, this.session.id)

        StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

        val io = if (publisher != null) {
            effect {
                val message = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

                val noticePublisher = publisher.plain

                val boardPublisher = noticePublisher
                    .replaceIf(config.swapType == SwapType.EDIT && message != null) { publisher.edit(message!!) }

                val messageBufferKey = session.messageBufferKey

                val result = session.gameResult!!
                val eloRating =
                    if (session is EngineGameSession) {
                        val delta = session.ratingDelta!!

                        tuple(session.userRating + delta, delta)
                    } else null

                service.buildGameFinished(
                    noticePublisher,
                    config.language.container,
                    ResultDraw(
                        session.users,
                        session.state.board.playerColor,
                        result,
                        eloRating,
                    )
                ).launch()()

                buildFinishProcedure(
                    bot,
                    service,
                    boardPublisher,
                    config,
                    session,
                    messageBufferKey
                )()

                service.archiveSession(session, config.archivePolicy)
            }
        } else effect { }

        val report = this.writeActionLog(emittedTime, "expired, ${session.gameResult!!}", channel)

        CommandResult(io, report)
    }

}
