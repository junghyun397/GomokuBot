package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import renju.notation.Pos
import utils.structs.IO
import utils.structs.flatMap
import utils.structs.fold
import utils.structs.map

class SetCommand(
    private val session: GameSession,
    private val pos: Pos,
    private val deployAt: MessageRef?,
    override val responseFlag: ResponseFlag,
) : Command {

    override val name = "set"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        if (session.board.validateMove(this.pos).isDefined) throw IllegalStateException()

        val thenSession = GameManager.makeMove(this.session, this.pos)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        thenSession.gameResult.fold(
            onEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessions, guild, thenSession)

                    val guideIO = when {
                        config.swapType == SwapType.EDIT && this.deployAt == null -> IO.empty
                        else -> {
                            val guidePublisher = when (config.swapType) {
                                SwapType.EDIT -> publishers.windowed
                                else -> publishers.plain
                            }

                            service.buildNextMovePVP(guidePublisher, config.language.container, thenSession.player, thenSession.nextPlayer, pos)
                                .retrieve()
                                .flatMap { buildAppendGameMessageProcedure(it, bot, thenSession) }
                        }
                    }

                    val io = guideIO
                        .flatMap { buildNextMoveProcedure(bot, guild, config,
                            service, boardPublisher, this.session, thenSession) }

                    io to this.writeCommandReport("make move $pos", guild, user)
                }
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(thenSession, bot.resRenjuClient)

                    nextSession.gameResult.fold(
                        onEmpty = {
                            SessionManager.putGameSession(bot.sessions, guild, nextSession)

                            val guideIO = when {
                                config.swapType == SwapType.EDIT && this.deployAt == null -> IO.empty
                                else -> {
                                    val guidePublisher = when (config.swapType) {
                                        SwapType.EDIT -> publishers.windowed
                                        else -> publishers.plain
                                    }

                                    service.buildNextMovePVE(guidePublisher, config.language.container, nextSession.owner, nextSession.board.lastPos().get())
                                        .retrieve()
                                        .flatMap { buildAppendGameMessageProcedure(it, bot, thenSession) }
                                }
                            }

                            val io = guideIO
                                .flatMap { buildNextMoveProcedure(bot, guild, config,
                                    service, boardPublisher, this.session, nextSession) }

                            io to this.writeCommandReport("make move $pos", guild, user)
                        },
                        onDefined = { result ->
                            GameManager.finishSession(bot, guild, nextSession, result)

                            val io = when (result) {
                                is GameResult.Win ->
                                    service.buildLosePVE(publishers.plain, config.language.container, nextSession.owner, nextSession.board.lastPos().get())
                                is GameResult.Full ->
                                    service.buildTiePVE(publishers.plain, config.language.container, nextSession.owner)
                            }
                                .launch()
                                .flatMap { buildFinishProcedure(bot,
                                    service, boardPublisher, config, this.session, nextSession) }
                                .map { it + Order.ArchiveSession(nextSession, config.archivePolicy) }

                            io to this.writeCommandReport("make move $pos, terminate session by $result", guild, user)
                        }
                    )
                }
            } },
            onDefined = { result ->
                GameManager.finishSession(bot, guild, thenSession, result)

                when (thenSession) {
                    is PvpGameSession -> {
                        val io = when (result) {
                            is GameResult.Win ->
                                service.buildWinPVP(publishers.plain, config.language.container, thenSession.nextPlayer, thenSession.player, pos)
                            is GameResult.Full ->
                                service.buildTiePVP(publishers.plain, config.language.container, thenSession.owner, thenSession.opponent)
                        }
                            .launch()
                            .flatMap { buildFinishProcedure(bot,
                                service, boardPublisher, config, this.session, thenSession) }
                            .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                        io to this.writeCommandReport("make move $pos, terminate session by $result", guild, user)
                    }
                    is AiGameSession -> {
                        val io = when (result) {
                            is GameResult.Win ->
                                service.buildWinPVE(publishers.plain, config.language.container, thenSession.owner, pos)
                            is GameResult.Full ->
                                service.buildTiePVE(publishers.plain, config.language.container, thenSession.owner)
                        }
                            .launch()
                            .flatMap { buildFinishProcedure(bot,
                                service, boardPublisher, config, this.session, thenSession) }
                            .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                        io to this.writeCommandReport("make move $pos, terminate session by $result", guild, user)
                    }
                }
            }
        )
    }

}
