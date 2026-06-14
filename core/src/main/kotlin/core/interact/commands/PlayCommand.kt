package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.EngineGameManager
import core.session.MessageManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.*
import renju.notation.GameResult
import renju.notation.Pos
import utils.tuple
import utils.unreachable

class PlayCommand(
    private val sessionId: SessionId,
    private val pos: Pos,
    override val responseFlag: ResponseFlag,
    private val messageRef: MessageRef?,
) : Command {

    override val name = "set"

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        var messageBufferKey: MessageBufferKey? = null

        val session = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { session ->
            messageBufferKey = session.messageBufferKey

            when (session) {
                is PvpGameSession -> PvpGameManager.play(session, this.pos)
                is EngineGameSession -> EngineGameManager.play(session, this.pos)
                else -> unreachable()
            }
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.messageRef ?: MessageManager.viewHeadMessage(bot.sessions, messageBufferKey!!)!!)
            else -> publishers.plain
        }

        when (val result = session.gameResult) {
            is GameResult -> {
                SessionManager.deleteGameSession(bot.sessions, this.sessionId)

                val lastMove = session.state.history.lastAction!!

                val io = effect {
                    when (session) {
                        is PvpGameSession -> when (result) {
                            is GameResult.Win ->
                                service.buildWinPvp(
                                    publishers.plain,
                                    config.language.container,
                                    session.opponent,
                                    session.player,
                                    lastMove
                                )
                            is GameResult.Full ->
                                service.buildTiePvp(publishers.plain, config.language.container, session.users)
                        }
                        is EngineGameSession -> when (result) {
                            is GameResult.Win -> when (result.winner) {
                                session.userColor ->
                                    service.buildEngineLose(publishers.plain, config.language.container, session.humanPlayer, lastMove)
                                else -> service.buildEngineWin(publishers.plain, config.language.container, session.humanPlayer, lastMove)
                            }
                            is GameResult.Full ->
                                service.buildEngineTie(publishers.plain, config.language.container, session.humanPlayer)
                        }
                        else -> unreachable()
                    }.launch()()

                    val orders = buildFinishProcedure(
                        bot,
                        service,
                        boardPublisher,
                        config,
                        session,
                        messageBufferKey!!
                    )()

                    orders + Order.ArchiveSession(session, config.archivePolicy)
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}, terminate session by $result", channel, user))
            }
            null -> {
                val guideIO = when {
                    config.swapType == SwapType.EDIT && this.messageRef == null -> effect { Unit }
                    else -> {
                        val guidePublisher = when (config.swapType) {
                            SwapType.EDIT -> publishers.windowed
                            else -> publishers.plain
                        }

                        effect {
                            val maybeGuideMessage = when (session) {
                                is PvpGameSession ->
                                    service.buildNextMovePvp(
                                        guidePublisher,
                                        config.language.container,
                                        session.opponent,
                                        this@PlayCommand.pos
                                    )
                                is EngineGameSession ->
                                    service.buildNextMoveEngine(
                                        guidePublisher,
                                        config.language.container,
                                        session.player,
                                        session.state.history.lastAction ?: this@PlayCommand.pos
                                    )
                                else -> unreachable()
                            }.retrieve()()

                            buildAppendGameMessageProcedure(maybeGuideMessage, bot, session)()
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
                        session,
                        messageBufferKey!!
                    )()
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}", channel, user))
            }
        }
    }

}
