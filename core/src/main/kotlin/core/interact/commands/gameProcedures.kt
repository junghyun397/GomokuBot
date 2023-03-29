package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.inference.FocusSolver
import core.interact.Order
import core.interact.message.MessageAdaptor
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.session.FocusType
import core.session.HintType
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import utils.lang.shift
import utils.lang.tuple
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
    service: MessagingService<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
    thenSession: GameSession,
): IO<List<Order>> = buildBoardProcedure(bot, guild, config, service, publisher, thenSession)
    .flatMap { buildSwapProcedure(bot, config, session) }

fun <A, B> buildBoardProcedure(
    bot: BotContext,
    guild: Guild,
    config: GuildConfig,
    service: MessagingService<A, B>,
    publisher: MessagePublisher<A, B>,
    session: GameSession,
): IO<Unit> {
    val focusInfo = when (config.focusType) {
        FocusType.INTELLIGENCE -> FocusSolver.resolveFocus(session.board, service.focusWidth, config.hintType == HintType.FIVE)
        FocusType.FALLOWING -> FocusSolver.resolveCenter(session.board, service.focusRange)
    }

    return service.buildBoard(publisher, config.language.container, config.boardStyle.renderer, config.markType, session)
        .shift(session.board.winner().isEmpty) { io ->
            when (session) {
                is SwapStageOpeningSession -> service.attachSwapButtons(io, config.language.container, session)
                is BranchingStageOpeningSession -> service.attachBranchingButtons(io, config.language.container)
                else -> service.attachFocusButtons(io, service.generateFocusedField(session, focusInfo))
            }
        }
        .retrieve()
        .flatMap { maybeMessage ->
            maybeMessage.fold(
                onDefined = { message ->
                    SessionManager.addNavigation(bot.sessions, message.messageRef, BoardNavigationState(focusInfo.focus.idx(), focusInfo, session.expireDate))
                    SessionManager.appendMessageHead(bot.sessions, session.messageBufferKey, message.messageRef)
                    service.attachFocusNavigators(message.messageData) {
                        SessionManager.retrieveGameSession(bot.sessions, guild, session.owner.id)
                            ?.board?.moves() != session.board.moves()
                    }
                },
                onEmpty = { IO.empty }
            )
        }
}

private fun buildSwapProcedure(
    bot: BotContext,
    config: GuildConfig,
    session: GameSession
): IO<List<Order>> = IO { when (config.swapType) {
    SwapType.RELAY -> listOf(Order.BulkDelete(SessionManager.checkoutMessages(bot.sessions, session.messageBufferKey).orEmpty()))
    SwapType.ARCHIVE -> {
        SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)
            ?.let { listOf(Order.RemoveNavigators(it, reduceComponents = true)) }
            ?: emptyList()
    }
    SwapType.EDIT -> emptyList()
} }

fun <A, B> buildFinishProcedure(
    bot: BotContext,
    service: MessagingService<A, B>,
    publisher: MessagePublisher<A, B>,
    config: GuildConfig,
    session: GameSession,
    thenSession: GameSession
): IO<List<Order>> = service.buildBoard(publisher, config.language.container, config.boardStyle.renderer, config.markType, thenSession)
    .retrieve()
    .flatMap { maybeMessage -> buildSwapProcedure(bot, config, session).map { tuple(maybeMessage, it) } }
    .map { (maybeMessage, originalOrder) ->
        maybeMessage
            .filter { thenSession.gameResult.isDefined && config.swapType == SwapType.EDIT }
            .fold(
                onDefined = { message -> originalOrder + Order.RemoveNavigators(message.messageRef, reduceComponents = true) },
                onEmpty = { originalOrder }
            )
    }
