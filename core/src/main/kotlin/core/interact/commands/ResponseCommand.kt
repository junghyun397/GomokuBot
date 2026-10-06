package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.gameStartedMessage
import core.interact.reports.writeActionLog
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.PvpGameSession
import core.session.entities.RequestSession
import core.session.entities.SessionId
import kotlin.time.Instant

class ResponseCommand(
    private val requestSessionId: SessionId,
    private val accept: Boolean,
) : Command {

    override val name = if (this.accept) "accept" else "reject"

    override val responseFlag = ResponseFlag.DeferEdit

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val requestSlot = SessionManager.retrieveRequestSession(this.requestSessionId)
        check(requestSlot.channelId == channel.id)
        val requestSession = requestSlot.snapshot()
        check(requestSession.recipient.id == user.id)

        val io = when (requestSession) {
            is RequestSession.Match -> requestSlot.interact { request ->
                if (this.accept) {
                    val session = PvpGameManager.create(requestSession.requester, requestSession.recipient, requestSession.rule)
                    SessionManager.insertGameSession(channel, session)
                    SessionManager.finishRequestSession(request)

                    val board = SessionManager.retrieveGameSession(session.id).interact { runtime ->
                        buildBoardProcedure(config, publishers, runtime)
                    }

                    effect {
                        val guidePublisher = request.messageRef?.let { publishers.edit(it) } ?: publishers.plain
                        guidePublisher(gameStartedMessage(config.language.container, service, session)).launch().bind()
                        board.bind()
                    }
                } else {
                    SessionManager.finishRequestSession(request)
                    val invalidate = buildInvalidateRequestProcedure(config, publishers, request)

                    effect {
                        invalidate.bind()
                        val notice = config.language.container.requestRejected(
                            service.formatUser(requestSession.requester),
                            service.formatUser(requestSession.recipient)
                        )

                        publishers.plain(AppMessage.Text(notice)).launch().bind()
                    }
                }
            }
            is RequestSession.Undo -> SessionManager.retrieveGameSession(requestSession.gameSessionId).interact { runtime ->
                requestSlot.interact { request ->
                    val session = runtime.session
                    check(session is PvpGameSession && session.gameResult == null)
                    check(runtime.undoRequest === request)

                    val nextSession = if (this.accept) PvpGameManager.undo(session) else session
                    SessionManager.finishUndoRequest(runtime)
                    runtime.session = nextSession

                    if (this.accept) {
                        val updateBoard = buildUpdateBoardProcedure(config, publishers, runtime)
                        effect {
                            val noticePublisher = request.messageRef?.let { publishers.edit(it) } ?: publishers.plain
                            noticePublisher(AppMessage.Text(config.language.container.undoPvpCompleted)).launch().bind()
                            updateBoard.bind()
                        }
                    } else {
                        val invalidate = buildInvalidateRequestProcedure(config, publishers, request)
                        effect {
                            invalidate.bind()
                            val notice = config.language.container.undoRequestRejected(
                                service.formatUser(requestSession.requester),
                                service.formatUser(requestSession.recipient)
                            )
                            publishers.plain(AppMessage.Text(notice)).launch().bind()
                        }
                    }
                }
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "${this.name} ${this.requestSessionId}", channel, user))
    }

}
