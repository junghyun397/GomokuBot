package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.*
import kotlin.time.Instant

class UndoCommand(
    private val sessionId: SessionId,
    override val responseFlag: ResponseFlag = ResponseFlag.DeferWindowed,
) : Command {

    override val name = "undo"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).interact { runtime ->
            val session = runtime.session
            check(session.users.black.id == user.id || session.users.white.id == user.id)

            check(session.gameResult == null)

            val forbidden = when {
                session is OpeningSession -> config.language.container.undoErrorOpening
                session is EngineGameSession && session.remainingUndos == 0 -> config.language.container.undoErrorLimit
                session.state.history.size < (if (session is EngineGameSession) 2 else 1) -> config.language.container.undoErrorNoMoves
                else -> null
            }

            if (forbidden != null) {
                effect {
                    publishers.windowed(AppMessage.Text(forbidden)).launch()()
                }
            } else if (session is PvpGameSession) {
                val invalidatePrevious = buildInvalidateUndoProcedure(bot.sessions, config, service, publishers, runtime)
                val request = PvpGameManager.requestUndo(session, user)
                SessionManager.createRequestSession(bot.sessions, channel, setOf(request.requester.id, request.recipient.id), request)
                val sendRequest = SessionManager.retrieveRequestSession(bot.sessions, request.id).interact { requestRuntime ->
                    runtime.undoRequest = requestRuntime
                    buildRequestProcedure(config, service, publishers, requestRuntime)
                }

                effect {
                    invalidatePrevious()
                    sendRequest()
                }
            } else {
                check(session is EngineGameSession)
                val nextSession = EngineGameManager.undo(session)
                runtime.session = nextSession
                val updateBoard = buildUpdateBoardProcedure(config, service, publishers, runtime)

                effect {
                    updateBoard()
                    val notice = config.language.container.undoCompleted(nextSession.remainingUndos)
                    publishers.windowed(AppMessage.Text(notice)).launch()()
                }
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "request undo", channel, user))
    }

}
