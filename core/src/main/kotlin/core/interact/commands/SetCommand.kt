package core.interact.commands

import core.BotContext
import core.assets.User
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.asCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.PvpGameSession
import jrenju.notation.Pos
import kotlinx.coroutines.Deferred

class SetCommand(override val command: String, private val session: GameSession, private val pos: Pos) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        user: User,
        message: Deferred<MessageAdaptor<A, B>>,
        producer: MessageProducer<A, B>,
        publisher: MessagePublisher<A, B>,
    ) = runCatching {
        val thenSession = GameManager.makeMove(this.session, this.pos)

        thenSession.gameResult.fold(
            onEmpty = { when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.putGameSession(bot.sessionRepository, config.id, thenSession)

                    val io = producer.produceNextMovePVP(publisher, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)
                        .attachNextMoveSequence(bot, config, producer, publisher, this.session, thenSession)

                    io to this.asCommandReport("make move ${pos.toCartesian()}", user)
                }
                is AiGameSession -> {
                    val nextSession = GameManager.makeAiMove(bot.b3NzeneClient, thenSession, Pos.fromIdx(thenSession.board.latestMove()))

                    nextSession.gameResult.fold(
                        onEmpty = {
                            SessionManager.putGameSession(bot.sessionRepository, config.id, nextSession)

                            val io = producer.produceNextMovePVE(publisher, config.language.container, nextSession.owner, nextSession.history.last())
                                .attachNextMoveSequence(bot, config, producer, publisher, this.session, nextSession)

                            io to this.asCommandReport("make move ${pos.toCartesian()}", user)
                        },
                        onDefined = { result ->
                            SessionManager.removeGameSession(bot.sessionRepository, config.id, this.session.owner.id)

                            val io = producer.produceLosePVE(publisher, config.language.container, nextSession.owner, this.pos)
                                .map { it.launch() }
                                .attachFinishSequence(bot, producer, publisher, config, this.session, nextSession)

                            io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                        }
                    )
                }
            } },
            onDefined = { result -> when (thenSession) {
                is PvpGameSession -> {
                    SessionManager.removeGameSession(bot.sessionRepository, config.id, this.session.owner.id)

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVP(publisher, config.language.container, thenSession.player, thenSession.nextPlayer, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVP(publisher, config.language.container, thenSession.owner, thenSession.opponent)
                    }
                        .map { it.launch() }
                        .attachFinishSequence(bot, producer, publisher, config, this.session, thenSession)

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                }
                is AiGameSession -> {
                    SessionManager.removeGameSession(bot.sessionRepository, config.id, this.session.owner.id)

                    val io = when (result) {
                        is GameResult.Win ->
                            producer.produceWinPVE(publisher, config.language.container, thenSession.owner, this.pos)
                        is GameResult.Full ->
                            producer.produceTiePVE(publisher, config.language.container, thenSession.owner)
                    }
                        .map { it.launch() }
                        .attachFinishSequence(bot, producer, publisher, config, this.session, thenSession)

                    io to this.asCommandReport("make move ${pos.toCartesian()}, terminate session by $result", user)
                }
            } }
        )
    }

}
