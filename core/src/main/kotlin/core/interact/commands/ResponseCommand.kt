package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.*
import kotlin.time.Instant

class ResponseCommand(
    private val requestSessionId: SessionId,
    private val accept: Boolean,
) : Command {

    override val name = if (this.accept) "accept" else "reject"

    override val responseFlag = ResponseFlag.DeferEdit

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val requestSlot = SessionManager.retrieveRequestSession(bot.sessions, this.requestSessionId)
        check(requestSlot.channelId == channel.id)
        val requestSession = requestSlot.snapshot()
        check(requestSession.recipient.id == user.id)

        val io = when (requestSession) {
            is RequestSession.Match -> requestSlot.interact { request ->
                if (this.accept) {
                    val session = PvpGameManager.create(requestSession.requester, requestSession.recipient, requestSession.rule)
                    SessionManager.insertGameSession(bot.sessions, channel, session)
                    SessionManager.finishRequestSession(bot.sessions, request)
                    val board = SessionManager.retrieveGameSession(bot.sessions, session.id).interact { runtime ->
                        buildBoardProcedure(config, service, publishers, runtime)
                    }

                    effect {
                        val guidePublisher = request.messageRef?.let { publishers.edit(it) } ?: publishers.plain
                        val players = session.users.map { service.formatUser(it) }
                        val notice = if (session is OpeningSession) config.language.container.beginOpening(players)
                        else config.language.container.beginPvp(players)
                        guidePublisher(AppMessage.Text(notice)).launch()()
                        board()
                    }
                } else {
                    SessionManager.finishRequestSession(bot.sessions, request)
                    val invalidate = buildInvalidateRequestProcedure(config, service, publishers, request)
                    effect {
                        invalidate()
                        val notice = config.language.container.requestRejected(service.formatUser(requestSession.requester), service.formatUser(requestSession.recipient))
                        publishers.plain(AppMessage.Text(notice)).launch()()
                    }
                }
            }
            is RequestSession.Undo -> SessionManager.retrieveGameSession(bot.sessions, requestSession.gameSessionId).interact { runtime ->
                requestSlot.interact { request ->
                    val session = runtime.session
                    check(session is PvpGameSession && session.gameResult == null)
                    check(runtime.undoRequest === request)
                    val nextSession = if (this.accept) PvpGameManager.undo(session) else session
                    SessionManager.finishUndoRequest(bot.sessions, runtime)
                    runtime.session = nextSession

                    if (this.accept) {
                        val updateBoard = buildUpdateBoardProcedure(config, service, publishers, runtime)
                        effect {
                            val noticePublisher = request.messageRef?.let { publishers.edit(it) } ?: publishers.plain
                            noticePublisher(AppMessage.Text(config.language.container.undoPvpCompleted)).launch()()
                            updateBoard()
                        }
                    } else {
                        val invalidate = buildInvalidateRequestProcedure(config, service, publishers, request)
                        effect {
                            invalidate()
                            val notice = config.language.container.undoRequestRejected(service.formatUser(requestSession.requester), service.formatUser(requestSession.recipient))
                            publishers.plain(AppMessage.Text(notice)).launch()()
                        }
                    }
                }
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "${this.name} ${this.requestSessionId}", channel, user))
    }

}
