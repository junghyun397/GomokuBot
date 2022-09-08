package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.session.FocusPolicy
import core.session.SessionManager
import core.session.SweepPolicy
import core.session.entities.*
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.assets.LinuxTime
import utils.lang.and
import utils.structs.*

fun <A, B> buildAppendGameMessageProcedure(
    maybeMessage: Option<MessageAdaptor<A, B>>,
    bot: BotContext,
    session: GameSession
): IO<Unit> = maybeMessage.fold(
    onDefined = { message ->
        IO { SessionManager.appendMessage(bot.sessions, session.messageBufferKey, message.messageRef) }
    },
    onEmpty = { IO.empty }
)

fun <A, B> buildNextMoveProcedure(
    bot: BotContext,
    guild: Guild,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
    thenSession: GameSession,
): IO<List<Order>> = buildBoardProcedure(bot, guild, config, producer, publisher, thenSession)
    .flatMap { buildSweepProcedure(bot, config, session) }

fun <A, B> buildBoardProcedure(
    bot: BotContext,
    guild: Guild,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
): IO<Option<Unit>> = run {
    val focus = when (config.focusPolicy) {
        FocusPolicy.INTELLIGENCE -> FocusSolver.resolveFocus(session.board, producer.focusWidth)
        FocusPolicy.FALLOWING -> session.board.latestPos().fold(
            { Renju.BOARD_CENTER_POS() },
            { Pos(it.row().coerceIn(producer.focusRange), it.col().coerceIn(producer.focusRange)) }
        )
    }

    val boardMessageIO = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, session)

    producer.attachFocusButtons(boardMessageIO, session, focus)
        .retrieve()
        .mapOption { it and focus }
}
    .flatMapOption { (message, pos) ->
        SessionManager.addNavigate(bot.sessions, message.messageRef, BoardNavigateState(pos.idx(), session.expireDate))
        SessionManager.appendMessageHead(bot.sessions, session.messageBufferKey, message.messageRef)
        producer.attachFocusNavigators(message) {
            SessionManager.retrieveGameSession(bot.sessions, guild, session.owner.id)?.board?.moves() != session.board.moves()
        }
    }

private fun buildSweepProcedure(
    bot: BotContext,
    config: GuildConfig,
    session: GameSession
): IO<List<Order>> = when (config.sweepPolicy) {
    SweepPolicy.RELAY -> IO { listOf(Order.BulkDelete(SessionManager.checkoutMessages(bot.sessions, session.messageBufferKey).orEmpty())) }
    SweepPolicy.LEAVE -> {
        SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
            ?.let { IO { listOf(Order.RemoveNavigators(it, true)) } }
            ?: IO { emptyList() }
    }
    SweepPolicy.EDIT -> IO { emptyList() }
}

fun <A, B> buildFinishProcedure(
    bot: BotContext,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    config: GuildConfig,
    session: GameSession,
    thenSession: GameSession
): IO<List<Order>> = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, thenSession)
    .launch()
    .flatMap { buildSweepProcedure(bot, config, session) }
    .map { originalOrder -> when {
        thenSession.gameResult.isDefined && config.sweepPolicy == SweepPolicy.EDIT ->
            SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
                ?.let { originalOrder + Order.RemoveNavigators(it) }
                ?: originalOrder
        else -> originalOrder
    } }

fun <A, B> buildHelpProcedure(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    producer: MessageProducer<A, B>,
): IO<Option<Unit>> = producer.produceHelp(publisher, config.language.container, 0)
    .retrieve()
    .flatMapOption { helpMessage ->
        SessionManager.addNavigate(
            bot.sessions,
            helpMessage.messageRef,
            PageNavigateState(
                helpMessage.messageRef,
                NavigationKind.ABOUT,
                0,
                LinuxTime.withOffset(bot.config.navigatorExpireOffset)
            )
        )

        producer.attachBinaryNavigators(helpMessage)
    }

fun <A, B> buildCombinedHelpProcedure(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    producer: MessageProducer<A, B>,
    settingsPage: Int
): IO<Option<Unit>> = IO.zip(
    producer.produceHelp(publisher, config.language.container, 0).retrieve(),
    producer.produceSettings(publisher, config, settingsPage).retrieve(),
)
    .map { (maybeHelp, maybeSettings) -> Option.zip(maybeHelp, maybeSettings) }
    .flatMapOption { (helpMessage, settingsMessage) ->
        SessionManager.addNavigate(
            bot.sessions,
            helpMessage.messageRef,
            PageNavigateState(
                helpMessage.messageRef,
                NavigationKind.ABOUT,
                0,
                LinuxTime.withOffset(bot.config.navigatorExpireOffset)
            )
        )

        SessionManager.addNavigate(
            bot.sessions,
            settingsMessage.messageRef,
            PageNavigateState(
                settingsMessage.messageRef,
                NavigationKind.SETTINGS,
                settingsPage,
                LinuxTime.withOffset(bot.config.navigatorExpireOffset)
            )
        )

        producer.attachBinaryNavigators(helpMessage)
            .flatMap { producer.attachBinaryNavigators(settingsMessage) }
    }
