package core.interact.parse

import arrow.core.Either
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.interact.message.AppMessage
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.GameSession
import core.session.entities.SessionId

abstract class SessionSideParser : CommandParser {

    context(sessions: SessionPool)
    protected fun retrieveSessionId(channel: Channel, user: User.Human): Either<ParseFailure, SessionId> =
        SessionManager.findGameSessionId(channel.id, user.id)?.let { Either.Right(it) }
            ?: Either.Left(ParseFailure(this.name, "$user session not found", channel, user) { messagingService, publisher, container ->
                effect {
                    publisher(AppMessage.Text(container.sessionNotFound))
                        .launch().bind()
                }
            })

    context(sessions: SessionPool)
    protected fun retrieveSession(channel: Channel, user: User.Human): Either<ParseFailure, Pair<SessionId, GameSession>> =
        this.retrieveSessionId(channel, user)
            .map { sessionId ->
                sessionId to SessionManager.retrieveGameSession(sessionId).snapshot()
            }

}
