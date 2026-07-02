package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.ResultDraw
import core.interact.reports.writeActionLog
import core.session.*
import core.session.entities.*
import utils.tuple
import kotlin.time.Instant

class ResignCommand(
    private val sessionId: SessionId,
) : Command {

    override val name = "resign"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val (session, messageBufferKey) = run {
            val staleSession = SessionManager.deleteGameSession(bot.sessions, this.sessionId)!!

            val finishedSession = when (staleSession) {
                is PvpGameSession -> PvpGameManager.resign(staleSession, user)
                is OpeningSession -> PvpGameManager.resign(staleSession, user)
                is EngineGameSession -> EngineGameManager.resign(staleSession, EngineGameManager.ResignCause.RESIGN)
            }

            tuple(finishedSession, staleSession.messageBufferKey)
        }

        val result = session.gameResult!!

        SessionManager.deleteGameSession(bot.sessions, this.sessionId)

        StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

        val publisher = run {
            val boardMessage = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            if (config.swapType == SwapType.EDIT && boardMessage != null)
                publishers.edit(boardMessage)
            else
                publishers.plain
        }

        val io = effect {
            val eloRating =
                if (session is EngineGameSession) {
                    val delta = session.ratingDelta!!

                    tuple(session.userRating + delta, delta)
                } else null

            service.buildGameFinished(
                publishers.plain,
                config.language.container,
                ResultDraw(
                    session.users,
                    session.state.board.playerColor,
                    result,
                    eloRating,
                )
            ).launch()()

            buildFinishProcedure(bot, service, publisher, config, session, messageBufferKey)()

            service.archiveSession(session, config.archivePolicy)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "resigned $result", channel, user))
    }

}
