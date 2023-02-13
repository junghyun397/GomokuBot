package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.SweepPolicy
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
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        if (session.board.validateMove(this.pos).isDefined) throw IllegalStateException()

        val thenSession = GameManager.makeMove(this.session, this.pos)

        val boardPublisher = when (config.sweepPolicy) {
            SweepPolicy.EDIT -> publishers.edit(this.deployAt ?: messageRef)
            else -> publishers.plain
        }

        thenSession.gameResult.fold(
            onEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessions, guild, thenSession)

                    val guideIO = when {
                        config.sweepPolicy == SweepPolicy.EDIT && this.deployAt == null -> IO.empty
                        else -> {
                            val guidePublisher = when (config.sweepPolicy) {
                                SweepPolicy.EDIT -> publishers.windowed
                                else -> publishers.plain
                            }

                            producer.produceNextMovePVP(guidePublisher, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)
                                .retrieve()
                                .flatMap { buildAppendGameMessageProcedure(it, bot, thenSession) }
                        }
                    }

                    val io = guideIO
                        .flatMap { buildNextMoveProcedure(bot, guild, config, producer, boardPublisher, this.session, thenSession) }

                    io to this.asCommandReport("make move $pos", guild, user)
                }
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(bot.resRenjuClient, thenSession)

                    nextSession.gameResult.fold(
                        onEmpty = {
                            SessionManager.putGameSession(bot.sessions, guild, nextSession)

                            val guideIO = when {
                                config.sweepPolicy == SweepPolicy.EDIT && this.deployAt == null -> IO.empty
                                else -> {
                                    val guidePublisher = when (config.sweepPolicy) {
                                        SweepPolicy.EDIT -> publishers.windowed
                                        else -> publishers.plain
                                    }

                                    producer.produceNextMovePVE(guidePublisher, config.language.container, nextSession.owner, nextSession.board.lastPos().get())
                                        .retrieve()
                                        .flatMap { buildAppendGameMessageProcedure(it, bot, thenSession) }
                                }
                            }

                            val io = guideIO
                                .flatMap { buildNextMoveProcedure(bot, guild, config, producer, boardPublisher, this.session, nextSession) }

                            io to this.asCommandReport("make move $pos", guild, user)
                        },
                        onDefined = { result ->
                            GameManager.finishSession(bot, guild, nextSession, result)

                            val io = when (result) {
                                is GameResult.Win ->
                                    producer.produceLosePVE(publishers.plain, config.language.container, nextSession.owner, nextSession.board.lastPos().get())
                                is GameResult.Full ->
                                    producer.produceTiePVE(publishers.plain, config.language.container, nextSession.owner)
                            }
                                .launch()
                                .flatMap { buildFinishProcedure(bot, producer, boardPublisher, config, this.session, nextSession) }
                                .map { it + Order.ArchiveSession(nextSession, config.archivePolicy) }

                            io to this.asCommandReport("make move $pos, terminate session by $result", guild, user)
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
                                producer.produceWinPVP(publishers.plain, config.language.container, thenSession.nextPlayer, thenSession.player, this.pos)
                            is GameResult.Full ->
                                producer.produceTiePVP(publishers.plain, config.language.container, thenSession.owner, thenSession.opponent)
                        }
                            .launch()
                            .flatMap { buildFinishProcedure(bot, producer, boardPublisher, config, this.session, thenSession) }
                            .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                        io to this.asCommandReport("make move $pos, terminate session by $result", guild, user)
                    }
                    is AiGameSession -> {
                        val io = when (result) {
                            is GameResult.Win ->
                                producer.produceWinPVE(publishers.plain, config.language.container, thenSession.owner, this.pos)
                            is GameResult.Full ->
                                producer.produceTiePVE(publishers.plain, config.language.container, thenSession.owner)
                        }
                            .launch()
                            .flatMap { buildFinishProcedure(bot, producer, boardPublisher, config, this.session, thenSession) }
                            .map { it + Order.ArchiveSession(thenSession, config.archivePolicy) }

                        io to this.asCommandReport("make move $pos, terminate session by $result", guild, user)
                    }
                }
            }
        )
    }

}
