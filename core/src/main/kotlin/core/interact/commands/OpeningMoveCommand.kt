package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.assets.humanId
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.ChannelConfig
import core.session.entities.GameSession
import core.session.entities.OpeningSession
import core.session.entities.SessionId
import renju.notation.Pos
import utils.lang.tuple

abstract class OpeningMoveCommand<T : OpeningSession>(
    private val sessionId: SessionId,
    protected val move: Pos,
    private val deployAt: MessageRef?,
    override val responseFlag: ResponseFlag
) : Command {

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet
    ) = runCatching {
        var session: T? = null
        val thenSession = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { currentSession ->
            val openingSession = this.selectSession(currentSession) ?: throw IllegalStateException()
            if (openingSession.player.humanId != user.id) throw IllegalStateException()
            if (GameManager.validateMove(openingSession, this.move) != null) throw IllegalStateException()

            session = openingSession
            this.executeSelf(openingSession)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val guideIO = when {
            config.swapType == SwapType.EDIT && this.deployAt == null -> effect { Unit }
            else -> {
                val guidePublisher = when (config.swapType) {
                    SwapType.EDIT -> publishers.windowed
                    else -> publishers.plain
                }

                effect {
                    val maybeGuideMessage = service.buildNextMoveOpening(guidePublisher, config.language.container, this@OpeningMoveCommand.move)
                        .retrieve()()

                    buildAppendGameMessageProcedure(maybeGuideMessage, bot, thenSession)()
                }
            }
        }

        val io = effect {
            guideIO()
            buildNextMoveProcedure(bot,
                channel, config, service, boardPublisher, session ?: throw IllegalStateException(), thenSession)()
        }

        tuple(io, this.writeCommandReport(this.writeLog(), channel, user))
    }

    protected abstract fun selectSession(session: GameSession): T?

    protected abstract fun executeSelf(session: T): GameSession

    protected abstract fun writeLog(): String

}
