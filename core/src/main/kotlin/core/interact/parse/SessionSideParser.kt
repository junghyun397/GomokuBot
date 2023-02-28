package core.interact.parse

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.session.SessionManager
import core.session.entities.GameSession
import utils.structs.Either
import utils.structs.map

abstract class SessionSideParser<A, B> : NamedParser {

    protected suspend fun retrieveSession(context: BotContext, guild: Guild, user: User): Either<GameSession, ParseFailure<A, B>> =
        SessionManager.retrieveGameSession(context.sessions, guild, user.id)?.let { Either.Left(it) }
            ?: Either.Right(ParseFailure(this.name, "$user session not found", guild, user) { messagingService, publisher, container ->
                messagingService.buildSessionNotFound(publisher, container)
                    .launch()
                    .map { emptyList() }
            })

}
