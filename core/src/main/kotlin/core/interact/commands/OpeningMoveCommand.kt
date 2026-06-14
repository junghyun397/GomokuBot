package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.MessageManager
import core.session.SessionManager
import core.session.entities.*
import renju.notation.Pos
import utils.tuple

abstract class OpeningMoveCommand<T : OpeningSession>(
    private val sessionId: SessionId,
    protected val move: Pos,
    override val responseFlag: ResponseFlag,
    private val messageRef: MessageRef?,
) : Command {

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet
    ) = runCatching {
        var messageBufferKey: MessageBufferKey? = null

        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { session ->
            val openingSession = this.selectSession(session) ?: throw IllegalStateException()
            if (openingSession.player.id != user.id) throw IllegalStateException()
            if (!openingSession.isLegalMove(this.move)) throw IllegalStateException()

            messageBufferKey = session.messageBufferKey
            this.executeSelf(openingSession)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.messageRef ?: MessageManager.viewHeadMessage(bot.sessions, messageBufferKey!!)!!)
            else -> publishers.plain
        }

        val guideIO = when {
            config.swapType == SwapType.EDIT && this.messageRef == null -> effect { Unit }
            else -> {
                val guidePublisher = when (config.swapType) {
                    SwapType.EDIT -> publishers.windowed
                    else -> publishers.plain
                }

                effect {
                    val maybeGuideMessage = service.buildNextMoveOpening(guidePublisher, config.language.container, this@OpeningMoveCommand.move)
                        .retrieve()()

                    buildAppendGameMessageProcedure(maybeGuideMessage, bot, session)()
                }
            }
        }

        val io = effect {
            guideIO()
            buildNextMoveProcedure(bot, config, service, boardPublisher, session, messageBufferKey!!)()
        }

        tuple(io, this.writeCommandReport(this.writeLog(), channel, user))
    }

    protected abstract fun selectSession(session: GameSession): T?

    protected abstract fun executeSelf(session: T): GameSession

    protected abstract fun writeLog(): String

}
