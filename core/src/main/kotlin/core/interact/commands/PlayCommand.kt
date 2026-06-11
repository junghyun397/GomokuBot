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
import core.session.entities.*
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

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet,
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
            if (renjuSession.state.board.validateMove(this.pos) != null) throw IllegalStateException()

            session = renjuSession
            val beforeHash = renjuSession.state.board.hashKey
            val thenSession = GameManager.makeMove(renjuSession, this.pos)

            when (thenSession) {
                is EngineGameSession ->
                    if (thenSession.gameResult == null) {
                        aiMoved = true
                        GameManager.makeAiMove(bot.mintakaServer, thenSession, beforeHash, this.pos)
                    } else {
                        thenSession
                    }
                is PvpGameSession -> thenSession
            }
        } as RenjuSession

        val result = finalSession.gameResult

        if (result == null) {
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
                                    service.buildNextMovePvp(
                                        guidePublisher,
                                        config.language.container,
                                        finalSession.opponent,
                                        this@PlayCommand.pos
                                    )
                                is EngineGameSession ->
                                    service.buildNextMoveEngine(
                                        guidePublisher,
                                        config.language.container,
                                        finalSession.player,
                                        finalSession.state.history.lastAction ?: this@PlayCommand.pos
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
                        config,
                        service,
                        boardPublisher,
                        session ?: throw IllegalStateException(),
                        finalSession
                    )()
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}", channel, user))
        } else {
                GameManager.finishSession(bot, channel, finalSession)

                val io = effect {
                    when (finalSession) {
                        is PvpGameSession -> when (result) {
                            is GameResult.Win ->
                                service.buildWinPvp(
                                    publishers.plain,
                                    config.language.container,
                                    finalSession.opponent,
                                    finalSession.player,
                                    this@PlayCommand.pos
                                )
                            is GameResult.FiveInRow ->
                                service.buildWinPvp(
                                    publishers.plain,
                                    config.language.container,
                                    finalSession.opponent,
                                    finalSession.player,
                                    this@PlayCommand.pos
                                )
                            is GameResult.Full ->
                                service.buildTiePvp(publishers.plain, config.language.container, finalSession.user)
                        }
                        is EngineGameSession -> when (result) {
                            is GameResult.Win ->
                                if (aiMoved)
                                    service.buildLoseEngine(
                                        publishers.plain,
                                        config.language.container,
                                        finalSession.humanPlayer,
                                        finalSession.state.history.lastAction ?: this@PlayCommand.pos
                                    )
                                else
                                    service.buildWinEngine(publishers.plain, config.language.container, finalSession.humanPlayer, this@PlayCommand.pos)
                            is GameResult.FiveInRow ->
                                service.buildWinEngine(publishers.plain, config.language.container, finalSession.humanPlayer, this@PlayCommand.pos)
                            is GameResult.Full ->
                                service.buildTieEngine(publishers.plain, config.language.container, finalSession.humanPlayer)
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
    }

}
