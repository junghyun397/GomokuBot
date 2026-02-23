package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import renju.notation.Pos
import utils.lang.tuple

class SetCommand(
    private val session: RenjuSession,
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
        if (session.board.validateMove(this.pos).isDefined()) throw IllegalStateException()

        val thenSession = GameManager.makeMove(this.session, this.pos)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        thenSession.gameResult.fold(
            ifEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessions, guild, thenSession)

                    val guideIO = when {
                        config.swapType == SwapType.EDIT && this.deployAt == null -> effect { Unit }
                        else -> {
                            val guidePublisher = when (config.swapType) {
                                SwapType.EDIT -> publishers.windowed
                                else -> publishers.plain
                            }

                            effect {
                                val maybeGuideMessage = service.buildNextMovePVP(
                                    guidePublisher,
                                    config.language.container,
                                    thenSession.player,
                                    thenSession.nextPlayer,
                                    pos
                                )
                                    .retrieve()()

                                buildAppendGameMessageProcedure(maybeGuideMessage, bot, thenSession)()
                            }
                        }
                    }

                    val io = effect {
                        guideIO()
                        buildNextMoveProcedure(
                            bot,
                            guild,
                            config,
                            service,
                            boardPublisher,
                            this@SetCommand.session,
                            thenSession
                        )()
                    }

                    tuple(io, this.writeCommandReport("make move $pos", guild, user))
                }
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(thenSession)

                    nextSession.gameResult.fold(
                        ifEmpty = {
                            SessionManager.putGameSession(bot.sessions, guild, nextSession)

                            val guideIO = when {
                                config.swapType == SwapType.EDIT && this.deployAt == null -> effect { Unit }
                                else -> {
                                    val guidePublisher = when (config.swapType) {
                                        SwapType.EDIT -> publishers.windowed
                                        else -> publishers.plain
                                    }

                                    effect {
                                        val maybeGuideMessage = service.buildNextMovePVE(
                                            guidePublisher,
                                            config.language.container,
                                            nextSession.owner,
                                            nextSession.board.lastPos().get()
                                        )
                                            .retrieve()()

                                        buildAppendGameMessageProcedure(maybeGuideMessage, bot, thenSession)()
                                    }
                                }
                            }

                            val io = effect {
                                guideIO()
                                buildNextMoveProcedure(
                                    bot,
                                    guild,
                                    config,
                                    service,
                                    boardPublisher,
                                    this@SetCommand.session,
                                    nextSession
                                )()
                            }

                            tuple(io, this.writeCommandReport("make move $pos", guild, user))
                        },
                        ifSome = { result ->
                            GameManager.finishSession(bot, guild, nextSession, result)

                            val io = effect {
                                when (result) {
                                    is GameResult.Win ->
                                        service.buildLosePVE(
                                            publishers.plain,
                                            config.language.container,
                                            nextSession.owner,
                                            nextSession.board.lastPos().get()
                                        )
                                    is GameResult.Full ->
                                        service.buildTiePVE(publishers.plain, config.language.container, nextSession.owner)
                                }.launch()()

                                val finishOrders = buildFinishProcedure(
                                    bot,
                                    service,
                                    boardPublisher,
                                    config,
                                    this@SetCommand.session,
                                    nextSession
                                )()

                                finishOrders + Order.ArchiveSession(nextSession, config.archivePolicy)
                            }

                            tuple(io, this.writeCommandReport("make move $pos, terminate session by $result", guild, user))
                        }
                    )
                }
            } },
            ifSome = { result ->
                GameManager.finishSession(bot, guild, thenSession, result)

                when (thenSession) {
                    is PvpGameSession -> {
                        val io = effect {
                            when (result) {
                                is GameResult.Win ->
                                    service.buildWinPVP(
                                        publishers.plain,
                                        config.language.container,
                                        thenSession.nextPlayer,
                                        thenSession.player,
                                        pos
                                    )
                                is GameResult.Full ->
                                    service.buildTiePVP(publishers.plain, config.language.container, thenSession.owner, thenSession.opponent)
                            }.launch()()

                            val finishOrders = buildFinishProcedure(
                                bot,
                                service,
                                boardPublisher,
                                config,
                                this@SetCommand.session,
                                thenSession
                            )()

                            finishOrders + Order.ArchiveSession(thenSession, config.archivePolicy)
                        }

                        tuple(io, this.writeCommandReport("make move $pos, terminate session by $result", guild, user))
                    }
                    is AiGameSession -> {
                        val io = effect {
                            when (result) {
                                is GameResult.Win ->
                                    service.buildWinPVE(publishers.plain, config.language.container, thenSession.owner, pos)
                                is GameResult.Full ->
                                    service.buildTiePVE(publishers.plain, config.language.container, thenSession.owner)
                            }.launch()()

                            val finishOrders = buildFinishProcedure(
                                bot,
                                service,
                                boardPublisher,
                                config,
                                this@SetCommand.session,
                                thenSession
                            )()

                            finishOrders + Order.ArchiveSession(thenSession, config.archivePolicy)
                        }

                        tuple(io, this.writeCommandReport("make move $pos, terminate session by $result", guild, user))
                    }
                }
            }
        )
    }

}
