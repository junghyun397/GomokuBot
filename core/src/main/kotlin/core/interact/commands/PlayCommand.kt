package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.ResultDraw
import core.interact.reports.writeActionLog
import core.session.*
import core.session.entities.*
import renju.notation.GameResult
import renju.notation.Pos
import utils.tuple
import utils.unreachable
import kotlin.time.Instant

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
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val (session, messageBufferKey) = SessionManager.retrieveGameSession(bot.sessions, this.sessionId).mutate { session ->
            val nextSession = when (session) {
                is PvpGameSession -> PvpGameManager.play(session, this.pos)
                is EngineGameSession -> EngineGameManager.play(session, this.pos)
                else -> unreachable()
            }

            tuple(nextSession, session.messageBufferKey)
        }

        val boardPublisher = when (config.swapType) {
            SwapType.EDIT -> publishers.edit(this.messageRef ?: MessageManager.viewHeadMessage(bot.sessions, messageBufferKey)!!)
            else -> publishers.plain
        }

        when (val result = session.gameResult) {
            is GameResult -> {
                SessionManager.deleteGameSession(bot.sessions, this.sessionId)

                StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

                val io = effect {
                    val eloRating =
                        if (session is EngineGameSession) {
                            val delta = session.ratingDelta!!

                            tuple(session.userRating + delta, delta)
                        } else null

                    service.buildGameFinished(
                        publishers.plain,
                        config.language.container,
                        ResultDraw(
                            session.users,
                            session.state.board.playerColor,
                            result,
                            eloRating,
                        )
                    ).launch()()

                    buildFinishProcedure(
                        bot,
                        service,
                        boardPublisher,
                        config,
                        session,
                        messageBufferKey
                    )()

                    service.archiveSession(session, config.archivePolicy)
                }

                CommandResult(io, this.writeActionLog(emittedTime, "make move ${this.pos}, finished $result", channel, user))
            }
            null -> {
                val guideIO = when {
                    config.swapType == SwapType.EDIT && this.messageRef == null -> effect { }
                    else -> {
                        val guidePublisher = when (config.swapType) {
                            SwapType.EDIT -> publishers.windowed
                            else -> publishers.plain
                        }

                        effect {
                            val maybeGuideMessage = when (session) {
                                is PvpGameSession ->
                                    service.buildMessage(
                                        guidePublisher,
                                        PlatformMessage(config.language.container.processNextPvp(
                                            service.formatUser(session.opponent),
                                            service.formatHighlight(this@PlayCommand.pos.toString())
                                        ))
                                    )
                                is EngineGameSession ->
                                    service.buildMessage(
                                        guidePublisher,
                                        PlatformMessage(config.language.container.processNextEngine(
                                            service.formatHighlight(
                                                (session.state.history.lastOrNull() ?: this@PlayCommand.pos).toString()
                                            )
                                        ))
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
                        messageBufferKey
                    )()
                }

                CommandResult(io, this.writeActionLog(emittedTime, "make move ${this.pos}", channel, user))
            }
        }
    }

}
