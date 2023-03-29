package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.OpeningSession
import renju.notation.Pos
import utils.lang.tuple
import utils.structs.IO
import utils.structs.flatMap

abstract class OpeningMoveCommand<T : OpeningSession>(
    protected val session: T,
    protected val move: Pos,
    private val deployAt: MessageRef?,
    override val responseFlag: ResponseFlag
) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val thenSession = this.executeSelf()

        SessionManager.putGameSession(bot.sessions, guild, thenSession)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        val guideIO = when {
            config.swapType == SwapType.EDIT && this.deployAt == null -> IO.empty
            else -> {
                val guidePublisher = when (config.swapType) {
                    SwapType.EDIT -> publishers.windowed
                    else -> publishers.plain
                }

                service.buildNextMoveOpening(guidePublisher, config.language.container, this.move)
                    .retrieve()
                    .flatMap { buildAppendGameMessageProcedure(it, bot, thenSession) }
            }
        }

        val io = guideIO
            .flatMap { buildNextMoveProcedure(bot, guild, config, service, boardPublisher, this.session, thenSession) }

        tuple(io, this.writeCommandReport(this.writeLog(), guild, user))
    }

    protected abstract fun executeSelf(): GameSession

    protected abstract fun writeLog(): String

}
