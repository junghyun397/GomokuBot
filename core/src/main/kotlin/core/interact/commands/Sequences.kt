package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageIO
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.session.FocusPolicy
import core.session.SessionManager
import core.session.SweepPolicy
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.NavigateState
import core.session.entities.NavigationKind
import jrenju.notation.Pos
import jrenju.notation.Renju
import utils.assets.LinuxTime
import utils.lang.and
import utils.structs.*

fun <A, B> buildNextMoveSequence(
    messageIO: MessageIO<A, B>,
    bot: BotContext,
    guild: Guild,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
    thenSession: GameSession,
) = messageIO
    .retrieve()
    .flatMapOption { IO { SessionManager.appendMessage(bot.sessions, thenSession.messageBufferKey, it.messageRef) } }
    .flatMap { buildBoardSequence(bot, guild, config, producer, publisher, thenSession) }
    .flatMap { buildSweepSequence(bot, config, session) }

fun <A, B> buildBoardSequence(
    bot: BotContext,
    guild: Guild,
    config: GuildConfig,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
) = run {
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
        SessionManager.addNavigate(
            bot.sessions, message.messageRef, NavigateState(NavigationKind.BOARD, pos.idx(), session.expireDate)
        )
        SessionManager.appendMessageHead(bot.sessions, session.messageBufferKey, message.messageRef)
        producer.attachFocusNavigators(message) {
            SessionManager.retrieveGameSession(bot.sessions, guild, session.owner.id) != session
        }
    }

private fun buildSweepSequence(
    bot: BotContext,
    config: GuildConfig,
    session: GameSession
) = when (config.sweepPolicy) {
    SweepPolicy.RELAY -> IO { listOf(Order.BulkDelete(session.messageBufferKey)) }
    SweepPolicy.LEAVE -> {
        SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
            ?.let { IO { listOf(Order.RemoveNavigators(it, true)) } }
            ?: IO { emptyList() }
    }
}

fun <A, B> buildFinishSequence(
    bot: BotContext,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    config: GuildConfig,
    session: GameSession,
    thenSession: GameSession
) = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, thenSession)
    .launch()
    .flatMap { buildSweepSequence(bot, config, session) }

fun <A, B> buildHelpSequence(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    producer: MessageProducer<A, B>,
) = producer.produceHelp(publisher, config.language.container, 0)
    .retrieve()
    .flatMapOption { helpMessage ->
        SessionManager.addNavigate(
            bot.sessions,
            helpMessage.messageRef,
            NavigateState(NavigationKind.ABOUT, 0, LinuxTime.withOffset(bot.config.navigatorExpireOffset))
        )

        producer.attachBinaryNavigators(helpMessage)
    }

fun <A, B> buildCombinedHelpSequence(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    producer: MessageProducer<A, B>,
    settingsPage: Int
) = IO.zip(
    producer.produceHelp(publisher, config.language.container, 0).retrieve(),
    producer.produceSettings(publisher, config, settingsPage).retrieve(),
)
    .map { (maybeHelp, maybeSettings) -> Option.zip(maybeHelp, maybeSettings) }
    .flatMapOption { (helpMessage, settingsMessage) ->
        SessionManager.addNavigate(
            bot.sessions,
            helpMessage.messageRef,
            NavigateState(
                NavigationKind.ABOUT,
                0,
                LinuxTime.withOffset(bot.config.navigatorExpireOffset)
            )
        )

        SessionManager.addNavigate(
            bot.sessions,
            settingsMessage.messageRef,
            NavigateState(
                NavigationKind.SETTINGS,
                settingsPage,
                LinuxTime.withOffset(bot.config.navigatorExpireOffset)
            )
        )

        producer.attachBinaryNavigators(helpMessage)
            .flatMap { producer.attachBinaryNavigators(settingsMessage) }
    }
