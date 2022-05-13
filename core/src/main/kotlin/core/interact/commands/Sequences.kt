package core.interact.commands

import core.BotContext
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageAction
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.session.FocusPolicy
import core.session.SessionManager
import core.session.SweepPolicy
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.NavigableKind
import core.session.entities.NavigateState
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.structs.IO

fun <A, B> IO<Unit>.attachBeginSequence(
    bot: BotContext,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
) = this
    .attachBoardSequence(bot, config, producer, publisher, session)

fun <A, B> IO<MessageAction<A, B>>.attachNextMoveSequence(
    bot: BotContext,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
    thenSession: GameSession,
) = this
    .map { SessionManager.appendMessage(bot.sessionRepository, thenSession.messageBufferKey, it.retrieve().message) }
    .attachBoardSequence(bot, config, producer, publisher, thenSession)
    .attachSweepSequence(bot, config, session)

fun <A, B> IO<*>.attachBoardSequence(
    bot: BotContext,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
) = this
    .flatMap { producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, session) }
    .map { action ->
        val focus = when (config.focusPolicy) {
            FocusPolicy.INTELLIGENCE -> FocusSolver.resolveFocus(session.board, producer.focusWidth)
            FocusPolicy.FALLOWING -> session.board.latestPos().fold(
                { Renju.BOARD_CENTER_POS() },
                { Pos(it.row().coerceIn(2, Renju.BOARD_WIDTH_MAX_IDX() - 2), it.col().coerceIn(2, Renju.BOARD_WIDTH_MAX_IDX() - 2)) }
            )
        }

        producer.attachFocusButtons(action, session, focus).retrieve() to focus
    }
    .flatMap {
        SessionManager.addNavigate(
            bot.sessionRepository, it.first.message, NavigateState(NavigableKind.BOARD, it.second.idx(), session.expireDate)
        )
        SessionManager.appendMessageHead(bot.sessionRepository, session.messageBufferKey, it.first.message)
        producer.attachFocusNavigators(it.first) {
            SessionManager.retrieveGameSession(bot.sessionRepository, config.id, session.owner.id) != session
        }
    }

private fun IO<Unit>.attachSweepSequence(
    bot: BotContext,
    config: GuildConfig,
    session: GameSession
) = this
    .flatMap { when (config.sweepPolicy) {
        SweepPolicy.RELAY -> IO { Order.BulkDelete(session.messageBufferKey) }
        SweepPolicy.LEAVE -> {
            SessionManager.getHeadMessage(bot.sessionRepository, session.messageBufferKey)
                ?.let { IO { Order.RemoveNavigators(it) } }
                ?: IO { Order.Unit }
        }
    } }

fun <A, B> IO<Unit>.attachFinishSequence(
    bot: BotContext,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    config: GuildConfig,
    session: GameSession,
    thenSession: GameSession
) = this
    .flatMap {
        producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, thenSession).map { it.launch() }
    }
    .attachSweepSequence(bot, config, session)
