package core

import core.inference.B3nzeneClient
import core.interact.message.MessageBinder
import core.session.SessionRepository

data class BotContext(
    val databaseConnection: core.database.DatabaseConnection,
    val b3NzeneClient: B3nzeneClient,
    val sessionRepository: SessionRepository,
)
