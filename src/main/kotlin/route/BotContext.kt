package route

import database.DatabaseConnection
import inference.B3nzeneClient
import session.SessionRepository

data class BotContext(
    val databaseConnection: DatabaseConnection,
    val b3NzeneClient: B3nzeneClient,
    val sessionRepository: SessionRepository,
)
