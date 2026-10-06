package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.Channel
import core.database.DatabaseConnection
import core.engine.FocusSolver
import core.interact.message.*
import core.session.SessionPool
import core.session.StatsManager
import core.session.entities.*

context(service: PlatformService)
private fun prepareBoardNavigation(
    config: ChannelConfig,
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

context(service: PlatformService)
fun buildBoardProcedure(
    config: ChannelConfig,
    publishers: PublisherSet,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val navigation = prepareBoardNavigation(config, runtime)
    val view = session.buildBoardView(config, navigation?.initialFocus)

    return effect {
        val publicationId = runtime.reserveMessagePublication()
        val message = publishers.plain(AppMessage.Board(view)).retrieve().bind() ?: return@effect
        if (runtime.recordPublishedMessage(publicationId, message.ref))
            service.attachInputFieldNavigators(message).bind()
    }
}

context(service: PlatformService)
fun buildUpdateBoardProcedure(
    config: ChannelConfig,
    publishers: PublisherSet,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val navigation = prepareBoardNavigation(config, runtime)
    val view = session.buildBoardView(config, navigation?.initialFocus)
    val messageRef = runtime.messageRef ?: return effect { }

    return effect {
        val message = publishers.edit(messageRef)(AppMessage.Board(view)).retrieve().bind()
        if (message != null && session.gameResult != null)
            service.reduceComponents(messageRef, reduceReactions = true, reduceComponents = true)
    }
}

context(dbConnection: DatabaseConnection, sessions: SessionPool, service: PlatformService)
fun buildFinishProcedure(
    config: ChannelConfig,
    channel: Channel,
    publishers: PublisherSet?,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val invalidateUndo = buildInvalidateUndoProcedure(config, publishers, runtime)
    val updateBoard = publishers?.let { buildUpdateBoardProcedure(config, it, runtime) }

    return effect {
        invalidateUndo.bind()
        StatsManager.uploadGameRecord(channel.id, session)

        if (publishers != null) {
            val rating = if (session is EngineGameSession) {
                val delta = session.ratingDelta!!
                session.userRating + delta to delta
            } else null

            val result = ResultDraw(session.users, session.state.board.playerColor, session.gameResult!!, rating)
            val container = config.language.container
            val players = session.formatPlayers(container, service)
            publishers.plain(gameFinishedMessage(container, players, result)).launch().bind()
            updateBoard!!.bind()
            if (session.state.history.size >= 20 && config.archivePolicy != ArchivePolicy.PRIVACY) {
                service.archive(AppMessage.BoardArchive(session.buildBoardDraw(config.archivePolicy == ArchivePolicy.BY_ANONYMOUS)))
            }
        }
    }
}
