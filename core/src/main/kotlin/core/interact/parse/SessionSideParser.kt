package core.interact.parse

import arrow.core.Either
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.session.SessionManager
import core.session.entities.GameSession

abstract class SessionSideParser<A, B> : CommandParser {

    protected suspend fun retrieveSession(context: BotContext, channel: Channel, user: User.Human): Either<ParseFailure<A, B>, GameSession> =
        SessionManager.retrieveGameSession(context.sessions, channel, user.id)?.let { Either.Right(it) }
            ?: Either.Left(ParseFailure(this.name, "$user session not found", channel, user) { messagingService, publisher, container ->
                effect {
                    messagingService.buildSessionNotFound(publisher, container)
                        .launch()()
                    emptyList()
                }
            })

}
