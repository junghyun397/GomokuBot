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
import core.session.entities.GameSession
import core.session.entities.EngineGameSession
import core.session.entities.ChannelConfig
import core.session.entities.PvpGameSession
import core.session.entities.RenjuSession
import core.session.entities.SessionId
import renju.notation.GameResult
import renju.notation.Pos
import utils.lang.tuple

class PlayCommand(
    private val sessionId: SessionId,
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
        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        var session: RenjuSession? = null
        var aiMoved = false
        val finalSession = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { currentSession ->
            val renjuSession = currentSession as? RenjuSession ?: throw IllegalStateException()
            if (renjuSession.player.humanId != user.id) throw IllegalStateException()
            if (renjuSession.board.validateMove(this.pos).isSome()) throw IllegalStateException()

            session = renjuSession
            val beforeHash = renjuSession.board.hashKey
            val thenSession = GameManager.makeMove(renjuSession, this.pos)

            when (thenSession) {
                is EngineGameSession ->
                    thenSession.gameResult.fold(
                        ifEmpty = {
                            aiMoved = true
                            GameManager.makeAiMove(bot.mintakaServer, thenSession, beforeHash, this.pos)
                        },
                        ifSome = { thenSession }
                    )
                is PvpGameSession -> thenSession
            }
        } as RenjuSession

        finalSession.gameResult.fold(
            ifEmpty = {
                val guideIO = when {
                    config.swapType == SwapType.EDIT && this.deployAt == null -> effect { Unit }
                    else -> {
                        val guidePublisher = when (config.swapType) {
                            SwapType.EDIT -> publishers.windowed
                            else -> publishers.plain
                        }

                        effect {
                            val maybeGuideMessage = when (finalSession) {
                                is PvpGameSession ->
                                    service.buildNextMovePVP(
                                        guidePublisher,
                                        config.language.container,
                                        finalSession.player,
                                        finalSession.nextPlayer,
                                        this@PlayCommand.pos
                                    )
                                is EngineGameSession ->
                                    service.buildNextMovePVE(
                                        guidePublisher,
                                        config.language.container,
                                        finalSession.owner,
                                        finalSession.history.lastAction ?: this@PlayCommand.pos
                                    )
                            }.retrieve()()

                            buildAppendGameMessageProcedure(maybeGuideMessage, bot, finalSession)()
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
                        session ?: throw IllegalStateException(),
                        finalSession
                    )()
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}", channel, user))
            },
            ifSome = { result ->
                GameManager.finishSession(bot, channel, finalSession, result)

                val io = effect {
                    when (finalSession) {
                        is PvpGameSession -> when (result) {
                            is GameResult.Win ->
                                service.buildWinPVP(
                                    publishers.plain,
                                    config.language.container,
                                    finalSession.nextPlayer,
                                    finalSession.player,
                                    this@PlayCommand.pos
                                )
                            is GameResult.FiveInRow ->
                                service.buildWinPVP(
                                    publishers.plain,
                                    config.language.container,
                                    finalSession.nextPlayer,
                                    finalSession.player,
                                    this@PlayCommand.pos
                                )
                            is GameResult.Full ->
                                service.buildTiePVP(publishers.plain, config.language.container, finalSession.blackPlayer, finalSession.whitePlayer)
                        }
                        is EngineGameSession -> when (result) {
                            is GameResult.Win ->
                                if (aiMoved)
                                    service.buildLosePVE(
                                        publishers.plain,
                                        config.language.container,
                                        finalSession.humanPlayer,
                                        finalSession.history.lastAction ?: this@PlayCommand.pos
                                    )
                                else
                                    service.buildWinPVE(publishers.plain, config.language.container, finalSession.humanPlayer, this@PlayCommand.pos)
                            is GameResult.FiveInRow ->
                                service.buildWinPVE(publishers.plain, config.language.container, finalSession.humanPlayer, this@PlayCommand.pos)
                            is GameResult.Full ->
                                service.buildTiePVE(publishers.plain, config.language.container, finalSession.humanPlayer)
                        }
                    }.launch()()

                    val finishOrders = buildFinishProcedure(
                        bot,
                        service,
                        boardPublisher,
                        config,
                        session ?: throw IllegalStateException(),
                        finalSession
                    )()

                    finishOrders + Order.ArchiveSession(finalSession, config.archivePolicy)
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}, terminate session by $result", channel, user))
            }
        )
    }

}
