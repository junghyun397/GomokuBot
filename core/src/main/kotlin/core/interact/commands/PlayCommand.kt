package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.EngineGameSession
import core.session.entities.PvpGameSession
import core.session.entities.SessionId
import renju.notation.Pos
import utils.unreachable
import kotlin.time.Instant

class PlayCommand(
    private val sessionId: SessionId,
    private val pos: Pos,
    override val responseFlag: ResponseFlag,
) : Command {

    override val name = "set"

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io = SessionManager.retrieveGameSession(this.sessionId).interact { runtime ->
            val previous = runtime.session
            check(previous.gameResult == null)
            check(previous.player.id == user.id)
            check(previous.isLegalMove(this.pos))

            val session = when (previous) {
                is PvpGameSession -> PvpGameManager.play(previous, this.pos)
                is EngineGameSession -> EngineGameManager.play(previous, this.pos)
                else -> unreachable()
            }
            runtime.session = session

            val invalidateUndo = buildInvalidateUndoProcedure(config, publishers, runtime)
            val updateGame = if (session.gameResult != null) {
                SessionManager.finishGameSession(runtime)
                buildFinishProcedure(config, channel, publishers, runtime)
            } else {
                val updateBoard = buildUpdateBoardProcedure(config, publishers, runtime)

                effect {
                    updateBoard.bind()
                    if ((this@PlayCommand.responseFlag as? ResponseFlag.Defer)?.edit != true) {
                        val notice = when (session) {
                            is PvpGameSession -> config.language.container.processNextPvp(
                                service.formatUser(session.opponent),
                                service.formatHighlight(this@PlayCommand.pos.toString())
                            )
                            is EngineGameSession -> config.language.container.processNextEngine(
                                service.formatHighlight(session.state.history.lastOrNull().toString())
                            )
                        }
                        publishers.windowed(AppMessage.Text(notice)).launch().bind()
                    }
                }
            }

            effect {
                invalidateUndo.bind()
                updateGame.bind()
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "make move ${this.pos}", channel, user))
    }

}
