package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.GameSession
import core.session.entities.OpeningSession
import core.session.entities.SessionId
import renju.notation.Pos
import kotlin.time.Instant

abstract class OpeningMoveCommand<T : OpeningSession>(
    private val sessionId: SessionId,
    protected val move: Pos,
    override val responseFlag: ResponseFlag,
) : Command {

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
            val session = this.selectSession(runtime.session) ?: throw IllegalStateException()
            check(session.player.id == user.id)
            check(session.isLegalMove(this.move))
            runtime.session = this.executeSelf(session)
            val updateBoard = buildUpdateBoardProcedure(config, service, publishers, runtime)

            effect {
                updateBoard()
                if ((this@OpeningMoveCommand.responseFlag as? ResponseFlag.Defer)?.edit != true) {
                    val notice = config.language.container.processNextOpening(service.formatHighlight(this@OpeningMoveCommand.move.toString()))
                    publishers.windowed(AppMessage.Text(notice)).launch()()
                }
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, this.writeLog(), channel, user))
    }

    protected abstract fun selectSession(session: GameSession): T?

    protected abstract fun executeSelf(session: T): GameSession

    protected abstract fun writeLog(): String

}
