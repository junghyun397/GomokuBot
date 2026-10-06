package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.engine.FocusSolver
import core.interact.message.*
import core.session.StatsManager
import core.session.entities.*

private fun prepareBoardNavigation(
    config: ChannelConfig,
    service: PlatformService,
    runtime: SessionRuntime<GameSession>,
): BoardNavigationState? {
    val session = runtime.session
    val navigation = if (session.gameResult == null) {
        val focus = when (config.focusType) {
            FocusType.INTELLIGENCE -> FocusSolver.resolveFocus(session.state, service.focusWidth, config.hintType == HintType.FIVE)
            FocusType.CENTER -> FocusSolver.resolveCenter(session.state, service.focusRange)
        }
        BoardNavigationState(focus)
    } else null

    runtime.boardNavigation = navigation
    return navigation
}

fun buildBoardProcedure(
    config: ChannelConfig,
    service: PlatformService,
    publishers: PublisherSet,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val navigation = prepareBoardNavigation(config, service, runtime)
    val view = session.buildBoardView(config, navigation?.initialFocus)

    return effect {
        val publicationId = runtime.reserveMessagePublication()
        val message = publishers.plain(AppMessage.Board(view)).retrieve()() ?: return@effect
        if (runtime.recordPublishedMessage(publicationId, message.ref))
            service.attachInputFieldNavigators(message)()
    }
}

fun buildUpdateBoardProcedure(
    config: ChannelConfig,
    service: PlatformService,
    publishers: PublisherSet,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val navigation = prepareBoardNavigation(config, service, runtime)
    val view = session.buildBoardView(config, navigation?.initialFocus)
    val messageRef = runtime.messageRef ?: return effect { }

    return effect {
        val message = publishers.edit(messageRef)(AppMessage.Board(view)).retrieve()()
        if (message != null && session.gameResult != null)
            service.reduceComponents(messageRef, reduceReactions = true, reduceComponents = true)
    }
}

fun buildFinishProcedure(
    bot: BotContext,
    channel: Channel,
    config: ChannelConfig,
    service: PlatformService,
    publishers: PublisherSet?,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val invalidateUndo = buildInvalidateUndoProcedure(bot.sessions, config, service, publishers, runtime)
    val updateBoard = publishers?.let { buildUpdateBoardProcedure(config, service, it, runtime) }

    return effect {
        invalidateUndo()
        StatsManager.uploadGameRecord(bot.dbConnection, channel.id, session)

        if (publishers != null) {
            val rating = if (session is EngineGameSession) {
                val delta = session.ratingDelta!!
                session.userRating + delta to delta
            } else null

            val result = ResultDraw(session.users, session.state.board.playerColor, session.gameResult!!, rating)
            publishers.plain(gameFinishedMessage(config.language.container, service, result)).launch()()
            updateBoard!!()
            if (session.state.history.size >= 20 && config.archivePolicy != ArchivePolicy.PRIVACY) {
                service.archive(AppMessage.BoardArchive(session.buildBoardDraw(config.archivePolicy == ArchivePolicy.BY_ANONYMOUS)))
            }
        }
    }
}
