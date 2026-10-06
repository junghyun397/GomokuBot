package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.rejectedRequestMessage
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.GameSession
import core.session.entities.RequestSession
import core.session.entities.SessionRuntime

context(service: PlatformService)
fun buildInvalidateRequestProcedure(
    config: ChannelConfig,
    publishers: PublisherSet,
    runtime: SessionRuntime<RequestSession>,
): Effect<Nothing, Unit> {
    val session = runtime.session
    val message = rejectedRequestMessage(
        config.language.container,
        session,
        service.formatUser(session.requester),
        service.formatUser(session.recipient),
    )

    return effect {
        runtime.messageRef?.let { publishers.edit(it)(message).retrieve()() }
    }
}

context(service: PlatformService)
fun buildRequestProcedure(
    config: ChannelConfig,
    publishers: PublisherSet,
    runtime: SessionRuntime<RequestSession>,
): Effect<Nothing, Unit> = effect {
    val session = runtime.session
    val cancelled = rejectedRequestMessage(
        config.language.container,
        session,
        service.formatUser(session.requester),
        service.formatUser(session.recipient),
    )
    val publicationId = runtime.reserveMessagePublication()
    val closed = runtime.closed
    val content = if (closed) cancelled else AppMessage.Request(config.language.container, session)
    val message = publishers.plain(content).retrieve()() ?: return@effect

    if (!runtime.recordPublishedMessage(publicationId, message.ref) && !closed)
        publishers.edit(message.ref)(cancelled).retrieve()()
}

context(sessions: SessionPool, service: PlatformService)
fun buildInvalidateUndoProcedure(
    config: ChannelConfig,
    publishers: PublisherSet?,
    runtime: SessionRuntime<GameSession>,
): Effect<Nothing, Unit> {
    val request = SessionManager.finishUndoRequest(runtime)
    return if (request != null && publishers != null)
        buildInvalidateRequestProcedure(config, publishers, request)
    else effect { }
}
