package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.session.FocusPolicy
import core.session.HintPolicy
import core.session.SessionManager
import core.session.SweepPolicy
import core.session.entities.*
import utils.assets.LinuxTime
import utils.lang.pair
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
): IO<Option<Unit>> {
    val focusInfo = when (config.focusPolicy) {
        FocusPolicy.INTELLIGENCE -> FocusSolver.resolveFocus(session.board, producer.focusWidth, config.hintPolicy == HintPolicy.FIVE)
        FocusPolicy.FALLOWING -> FocusSolver.resolveCenter(session.board, producer.focusRange)
    }

    val boardMessageIO = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, session)

    val buttonBoardIO = if (session.board.winner().isEmpty)
        producer.attachFocusButtons(boardMessageIO, session, focusInfo)
    else
        boardMessageIO

    return buttonBoardIO
        .retrieve()
        .flatMapOption { message ->
            SessionManager.addNavigation(bot.sessions, message.messageRef, BoardNavigationState(focusInfo.focus.idx(), focusInfo, session.expireDate))
            SessionManager.appendMessageHead(bot.sessions, session.messageBufferKey, message.messageRef)
            producer.attachFocusNavigators(message.messageData) {
                SessionManager.retrieveGameSession(bot.sessions, guild, session.owner.id)?.board?.moves() != session.board.moves()
            }
        }
}


private fun buildSweepProcedure(
    bot: BotContext,
    config: GuildConfig,
    session: GameSession
): IO<List<Order>> = IO { when (config.sweepPolicy) {
    SweepPolicy.RELAY -> listOf(Order.BulkDelete(SessionManager.checkoutMessages(bot.sessions, session.messageBufferKey).orEmpty()))
    SweepPolicy.LEAVE -> {
        SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
            ?.let { listOf(Order.RemoveNavigators(it, reduceComponents = true)) }
            ?: emptyList()
    }
    SweepPolicy.EDIT -> emptyList()
} }

fun <A, B> buildFinishProcedure(
    bot: BotContext,
    producer: MessageProducer<A, B>,
    publisher: MessagePublisher<A, B>,
    config: GuildConfig,
    session: GameSession,
    thenSession: GameSession
): IO<List<Order>> = producer.produceBoard(publisher, config.language.container, config.boardStyle.renderer, thenSession)
    .retrieve()
    .flatMap { maybeMessage -> buildSweepProcedure(bot, config, session).map { maybeMessage pair it } }
    .map { (maybeMessage, originalOrder) ->
        maybeMessage
            .filter { thenSession.gameResult.isDefined && config.sweepPolicy == SweepPolicy.EDIT }
            .fold(
                onDefined = { message -> originalOrder + Order.RemoveNavigators(message.messageRef, reduceComponents = true) },
                onEmpty = { originalOrder }
            )
    }

fun <A, B> buildHelpProcedure(
    bot: BotContext,
    config: GuildConfig,
    publisher: MessagePublisher<A, B>,
    producer: MessageProducer<A, B>,
): IO<Option<Unit>> = producer.produceHelp(publisher, config.language.container, 0)
    .retrieve()
    .flatMapOption { helpMessage ->
        SessionManager.addNavigation(
            bot.sessions,
            helpMessage.messageRef,
            PageNavigationState(
                helpMessage.messageRef,
                NavigationKind.ABOUT,
                0,
                LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
            )
        )

        producer.attachBinaryNavigators(helpMessage.messageData)
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
        SessionManager.addNavigation(
            bot.sessions,
            helpMessage.messageRef,
            PageNavigationState(
                helpMessage.messageRef,
                NavigationKind.ABOUT,
                0,
                LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
            )
        )

        SessionManager.addNavigation(
            bot.sessions,
            settingsMessage.messageRef,
            PageNavigationState(
                settingsMessage.messageRef,
                NavigationKind.SETTINGS,
                settingsPage,
                LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
            )
        )

        producer.attachBinaryNavigators(helpMessage.messageData)
            .flatMap { producer.attachBinaryNavigators(settingsMessage.messageData) }
    }
