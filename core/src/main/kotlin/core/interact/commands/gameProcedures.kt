package core.interact.commands

import core.session.MessageManager
import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.mintaka.FocusSolver
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.session.FocusType
import core.session.HintType
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import utils.lang.replaceIf

fun <A, B> buildAppendGameMessageProcedure(
    maybeMessage: MessageAdaptor<A, B>?,
    bot: BotContext,
    session: GameSession
): Effect<Nothing, Unit> = effect {
    if (maybeMessage != null) {
        MessageManager.appendMessage(bot.sessions, session.messageBufferKey, maybeMessage.messageRef)
    }
}

fun <A, B> buildNextMoveProcedure(
    bot: BotContext,
    channel: Channel,
    config: ChannelConfig,
    service: MessagingService<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
    thenSession: GameSession,
): Effect<Nothing, List<Order>> = effect {
    buildBoardProcedure(bot, channel, config, service, publisher, thenSession)()
    buildSwapProcedure(bot, config, session)()
}

fun <A, B> buildBoardProcedure(
    bot: BotContext,
    channel: Channel,
    config: ChannelConfig,
    service: MessagingService<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
): Effect<Nothing, Unit> {
    val focusInfo = when (config.focusType) {
        FocusType.INTELLIGENCE -> FocusSolver.resolveFocus(session.state, service.focusWidth, config.hintType == HintType.FIVE)
        FocusType.CENTER -> FocusSolver.resolveCenter(session.state, service.focusRange)
    }

    return effect {
        val maybeMessage = service.buildBoard(publisher, config.language.container, config.boardStyle.renderer, config.markType, session)
            .replaceIf(session.board.winner() == null) { io -> io.addComponents(
                when (session) {
                    is SwapStageOpeningSession -> service.buildSwapButtons(config.language.container)
                    is BranchingStageOpeningSession -> service.buildBranchingButtons(config.language.container)
                    is DeclareStageOpeningSession -> service.buildDeclareButtons(config.language.container, session)
                    else -> service.buildFocusedButtons(service.generateFocusedField(session, focusInfo))
                }
            ) }
            .retrieve()()

        if (maybeMessage != null) {
            MessageManager.addNavigation(bot.sessions, maybeMessage.messageRef, BoardNavigationState(focusInfo.focus.idx, focusInfo, session.expireDate))
            MessageManager.appendMessageHead(bot.sessions, session.messageBufferKey, maybeMessage.messageRef)
            service.attachFocusNavigators(maybeMessage.messageData) {
                runCatching {
                    val currentSession = SessionManager.retrieveGameSession(bot.sessions, session.id).snapshot()
                    currentSession.history.moves != session.history.moves
                }.getOrElse { true }
            }()
        }
    }
}

private fun buildSwapProcedure(
    bot: BotContext,
    config: ChannelConfig,
    session: GameSession
): Effect<Nothing, List<Order>> = effect { when (config.swapType) {
    SwapType.RELAY -> listOf(Order.BulkDelete(MessageManager.checkoutMessages(bot.sessions, session.messageBufferKey).orEmpty()))
    SwapType.ARCHIVE -> {
        MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
            ?.let { listOf(Order.RemoveNavigators(it, reduceComponents = true)) }
            ?: emptyList()
    }
    SwapType.EDIT -> emptyList()
} }

fun <A, B> buildFinishProcedure(
    bot: BotContext,
    service: MessagingService<A, B>,
    publisher: MessagePublisher<A, B>,
    config: ChannelConfig,
    session: GameSession,
    thenSession: GameSession
): Effect<Nothing, List<Order>> = effect {
    val maybeMessage = service.buildBoard(publisher, config.language.container, config.boardStyle.renderer, config.markType, thenSession)
        .retrieve()()

    val originalOrder = buildSwapProcedure(bot, config, session)()

    if (maybeMessage != null && thenSession.gameResult != null && config.swapType == SwapType.EDIT)
        originalOrder + Order.RemoveNavigators(maybeMessage.messageRef, reduceComponents = true)
    else
        originalOrder
}
