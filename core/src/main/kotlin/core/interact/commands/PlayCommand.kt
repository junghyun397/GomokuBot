package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.assets.humanId
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.EngineGameSession
import core.session.entities.ChannelConfig
import core.session.entities.PvpGameSession
import core.session.entities.RenjuSession
import renju.notation.GameResult
import renju.notation.Pos
import utils.lang.tuple

class PlayCommand(
    private val pos: Pos,
    private val deployAt: MessageRef?,
    override val responseFlag: ResponseFlag,
) : Command {

    override val name = "set"

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val session = SessionManager.retrieveGameSession(bot.sessions, channel, user.id) as? RenjuSession
            ?: throw IllegalStateException()

        if (session.player.humanId != user.id) throw IllegalStateException()
        if (session.board.validateMove(this.pos).isSome()) throw IllegalStateException()

        val beforeHash = session.board.hashKey

        val thenSession = GameManager.makeMove(session, this.pos)

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        thenSession.gameResult.fold(
            ifEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessions, channel, thenSession)

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
                            channel,
                            config,
                            service,
                            boardPublisher,
                            session,
                            thenSession
                        )()
                    }

                    tuple(io, this.writeCommandReport("make move $pos", channel, user))
                }
                is EngineGameSession -> {
                    val nextSession = GameManager.makeAiMove(bot.mintakaServer, thenSession, beforeHash, pos)

                    nextSession.gameResult.fold(
                        ifEmpty = {
                            SessionManager.putGameSession(bot.sessions, channel, nextSession)

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
                                            nextSession.history.lastAction ?: this@PlayCommand.pos
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
                                    channel,
                                    config,
                                    service,
                                    boardPublisher,
                                    session,
                                    nextSession
                                )()
                            }

                            tuple(io, this.writeCommandReport("make move $pos", channel, user))
                        },
                        ifSome = { result ->
                            GameManager.finishSession(bot, channel, nextSession, result)

                            val io = effect {
                                when (result) {
                                    is GameResult.Win ->
                                        service.buildLosePVE(
                                            publishers.plain,
                                            config.language.container,
                                            nextSession.owner,
                                            nextSession.history.lastAction ?: this@PlayCommand.pos
                                        )
                                    is GameResult.FiveInRow ->
                                        service.buildLosePVE(
                                            publishers.plain,
                                            config.language.container,
                                            nextSession.owner,
                                            nextSession.history.lastAction ?: this@PlayCommand.pos
                                        )
                                    is GameResult.Full ->
                                        service.buildTiePVE(publishers.plain, config.language.container, nextSession.owner)
                                }.launch()()

                                val finishOrders = buildFinishProcedure(
                                    bot,
                                    service,
                                    boardPublisher,
                                    config,
                                    session,
                                    nextSession
                                )()

                                finishOrders + Order.ArchiveSession(nextSession, config.archivePolicy)
                            }

                            tuple(io, this.writeCommandReport("make move $pos, terminate session by $result",
                                channel, user))
                        }
                    )
                }
            } },
            ifSome = { result ->
                GameManager.finishSession(bot, channel, thenSession, result)

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
                                is GameResult.FiveInRow ->
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
                                session,
                                thenSession
                            )()

                            finishOrders + Order.ArchiveSession(thenSession, config.archivePolicy)
                        }

                        tuple(io, this.writeCommandReport("make move $pos, terminate session by $result", channel, user))
                    }
                    is EngineGameSession -> {
                        val io = effect {
                            when (result) {
                                is GameResult.Win ->
                                    service.buildWinPVE(publishers.plain, config.language.container, thenSession.owner, pos)
                                is GameResult.FiveInRow ->
                                    service.buildWinPVE(publishers.plain, config.language.container, thenSession.owner, pos)
                                is GameResult.Full ->
                                    service.buildTiePVE(publishers.plain, config.language.container, thenSession.owner)
                            }.launch()()

                            val finishOrders = buildFinishProcedure(
                                bot,
                                service,
                                boardPublisher,
                                config,
                                session,
                                thenSession
                            )()

                            finishOrders + Order.ArchiveSession(thenSession, config.archivePolicy)
                        }

                        tuple(io, this.writeCommandReport("make move $pos, terminate session by $result", channel, user))
                    }
                }
            }
        )
    }

}
