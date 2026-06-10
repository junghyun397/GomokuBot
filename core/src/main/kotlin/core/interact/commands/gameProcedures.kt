package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.interact.Order
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.interact.message.SentMessage
import core.mintaka.FocusSolver
import core.session.*
import core.session.entities.*
import utils.lang.replaceIf

fun buildAppendGameMessageProcedure(
    message: SentMessage?,
    bot: BotContext,
    session: GameSession
): Effect<Nothing, Unit> = effect {
    if (message != null) {
        MessageManager.appendMessage(bot.sessions, session.messageBufferKey, message.ref)
    }
}

fun buildNextMoveProcedure(
    bot: BotContext,
    channel: Channel,
    config: ChannelConfig,
    service: MessagingService,
    publisher: MessagePublisher,
    session: GameSession,
    thenSession: GameSession,
): Effect<Nothing, List<Order>> = effect {
    buildBoardProcedure(bot, channel, config, service, publisher, thenSession)()
    buildSwapProcedure(bot, config, session)()
}

fun buildBoardProcedure(
    bot: BotContext,
    channel: Channel,
    config: ChannelConfig,
    service: MessagingService,
    publisher: MessagePublisher,
    session: GameSession,
): Effect<Nothing, Unit> {
    val focusInfo = when (config.focusType) {
        FocusType.INTELLIGENCE -> FocusSolver.resolveFocus(session.state, service.focusWidth, config.hintType == HintType.FIVE)
        FocusType.CENTER -> FocusSolver.resolveCenter(session.state, service.focusRange)
    }

    return effect {
        val message = service.buildBoard(publisher, config.language.container, config.boardStyle.renderer, config.markType, session)
            .replaceIf(session.board.winner() == null) { io -> io.addComponents(
                when (session) {
                    is SwapStageOpeningSession -> service.buildSwapButtons(config.language.container)
                    is BranchingStageOpeningSession -> service.buildBranchingButtons(config.language.container)
                    is DeclareStageOpeningSession -> service.buildDeclareButtons(config.language.container, session)
                    else -> service.buildFocusedButtons(service.generateFocusedField(session, focusInfo))
                }
            ) }
            .retrieve()()

        if (message != null) {
            MessageManager.addNavigation(bot.sessions, message.ref, BoardNavigationState(focusInfo.focus.idx, focusInfo, session.expireDate))
            MessageManager.appendMessageHead(bot.sessions, session.messageBufferKey, message.ref)
            service.attachFocusNavigators(message) {
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

fun buildFinishProcedure(
    bot: BotContext,
    service: MessagingService,
    publisher: MessagePublisher,
    config: ChannelConfig,
    session: GameSession,
    thenSession: GameSession
): Effect<Nothing, List<Order>> = effect {
    val message = service.buildBoard(publisher, config.language.container, config.boardStyle.renderer, config.markType, thenSession)
        .retrieve()()

    val originalOrder = buildSwapProcedure(bot, config, session)()

    if (message != null && thenSession.gameResult != null && config.swapType == SwapType.EDIT)
        originalOrder + Order.RemoveNavigators(message.ref, reduceComponents = true)
    else
        originalOrder
}
