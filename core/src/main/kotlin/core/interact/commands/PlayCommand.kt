package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.*
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
        service: PlatformService,
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

                StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

                val lastMove = session.state.history.last()!!

                val io = effect {
                    when (session) {
                        is PvpGameSession -> when (result) {
                            is GameResult.Win ->
                                service.buildMessage(
                                    publishers.plain,
                                    PlatformMessage(config.language.container.endPvpWin(
                                        service.formatUser(session.opponent),
                                        service.formatUser(session.player),
                                        service.formatHighlight(lastMove.toString())
                                    ))
                                )
                            is GameResult.Full ->
                                service.buildMessage(
                                    publishers.plain,
                                    PlatformMessage(config.language.container.endPvpTie(session.users.map { service.formatUser(it) }))
                                )
                        }
                        is EngineGameSession -> when (result) {
                            is GameResult.Win -> when (result.winner) {
                                session.userColor ->
                                    service.buildMessage(
                                        publishers.plain,
                                        PlatformMessage(config.language.container.endEngineWin(
                                            service.formatUser(session.humanPlayer),
                                            service.formatHighlight(lastMove.toString())
                                        ))
                                    )
                                else ->
                                    service.buildMessage(
                                        publishers.plain,
                                        PlatformMessage(config.language.container.endEngineLose(
                                            service.formatUser(session.humanPlayer),
                                            service.formatHighlight(lastMove.toString())
                                        ))
                                    )
                            }
                            is GameResult.Full ->
                                service.buildMessage(
                                    publishers.plain,
                                    PlatformMessage(config.language.container.endEngineTie(service.formatUser(session.humanPlayer)))
                                )
                        }
                        else -> unreachable()
                    }.launch()()

                    buildFinishProcedure(
                        bot,
                        service,
                        boardPublisher,
                        config,
                        session,
                        messageBufferKey!!
                    )()

                    service.archiveSession(session, config.archivePolicy)
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}, terminate session by $result", channel, user))
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
                        messageBufferKey!!
                    )()
                }

                tuple(io, this.writeCommandReport("make move ${this.pos}", channel, user))
            }
        }
    }

}
