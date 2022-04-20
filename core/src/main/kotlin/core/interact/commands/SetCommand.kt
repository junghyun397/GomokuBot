package core.interact.commands

import core.BotContext
import core.assets.User
import core.assets.aiUser
import core.interact.Order
import core.interact.message.MessageModifier
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.SweepPolicy
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import jrenju.notation.Pos
import utils.structs.IO

class SetCommand(override val command: String, private val session: GameSession, private val pos: Pos) : Command {

    private fun <A, B> IO<Unit>.attachFinishSequence(
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        config: GuildConfig,
        thenSession: GameSession
    ) = this
        .flatMap { producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, thenSession) }
        .map { it.launch(); Order.BulkDelete(this@SetCommand.session.messageBufferKey) }

    private suspend fun <A, B> processNext(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        thenSession: GameSession,
        priorPlayer: User,
        player: User
    ): Pair<IO<Order>, CommandReport> {
        SessionManager.putGameSession(context.sessionRepository, config.id, thenSession)

        val io = producer.produceNextMove(publisher, config.language.container, player, priorPlayer, pos)
            .map { SessionManager.appendMessage(context.sessionRepository, thenSession.messageBufferKey, it.retrieve()) }
            .flatMap { producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, thenSession) }
            .map { producer.attachFocusButtons(it, config.language.container, config.focusPolicy, thenSession) }
            .map { SessionManager.appendMessage(context.sessionRepository, thenSession.messageBufferKey, it.retrieve()) }
            .map { when (config.sweepPolicy) {
                SweepPolicy.RELAY -> Order.BulkDelete(this.session.messageBufferKey)
                SweepPolicy.LEAVE -> Order.Unit
            } }

        return io to this.asCommandReport("make move ${pos.toCartesian()}", user)
    }

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
        modifier: MessageModifier<A, B>,
    ) = runCatching {
        val thenSession = GameManager.makeMove(this.session, this.pos)

        thenSession.gameResult.fold(
            onEmpty = { when (thenSession) {
                is PvpGameSession -> this.processNext(
                    context, config, user, producer, publisher,
                    thenSession, thenSession.priorPlayer, thenSession.player
                )
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(thenSession, Pos.fromIdx(thenSession.board.latestMove()))

                    nextSession.gameResult.fold(
                        onEmpty = {
                            this.processNext(
                                context, config, user, producer, publisher,
                                nextSession, aiUser, nextSession.owner
                            )
                        },
                        onDefined = { result ->
                            SessionManager.removeGameSession(context.sessionRepository, config.id, this.session.owner.id)

                            val io = producer.produceLosePVE(publisher, config.language.container, nextSession.owner, this.pos)
                                .map { it.launch() }
                                .attachFinishSequence(producer, publisher, config, nextSession)

                            io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by AI WIN", user)
                        }
                    )
                }
            } },
            onDefined = { result -> when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.removeGameSession(context.sessionRepository, config.id, this.session.owner.id)

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVP(publisher, config.language.container, thenSession.player, thenSession.priorPlayer, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVP(publisher, config.language.container, thenSession.owner, thenSession.opponent)
                    }
                        .map { it.launch() }
                        .attachFinishSequence(producer, publisher, config, thenSession)

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                }
                is AiGameSession -> {
                    SessionManager.removeGameSession(context.sessionRepository, config.id, this.session.owner.id)

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVE(publisher, config.language.container, thenSession.owner, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVE(publisher, config.language.container, thenSession.owner)
                    }
                        .map { it.launch() }
                        .attachFinishSequence(producer, publisher, config, thenSession)

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by ", user)
                }
            } }
        )
    }

}
