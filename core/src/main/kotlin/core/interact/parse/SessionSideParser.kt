package core.interact.parse

import arrow.core.Either
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.session.SessionManager
import core.session.entities.GameSession
import core.session.entities.SessionId

abstract class SessionSideParser<A, B> : CommandParser {

    protected fun retrieveSessionId(context: BotContext, channel: Channel, user: User.Human): Either<ParseFailure<A, B>, SessionId> =
        SessionManager.findGameSessionId(context.sessions, channel.id, user.id)?.let { Either.Right(it) }
            ?: Either.Left(ParseFailure(this.name, "$user session not found", channel, user) { messagingService, publisher, container ->
                effect {
                    messagingService.buildSessionNotFound(publisher, container)
                        .launch()()
                    emptyList()
                }
            })

    protected fun retrieveSession(context: BotContext, channel: Channel, user: User.Human): Either<ParseFailure<A, B>, Pair<SessionId, GameSession>> =
        this.retrieveSessionId(context, channel, user)
            .map { sessionId ->
                sessionId to SessionManager.retrieveGameSession(context.sessions, sessionId).snapshot()
            }

}
