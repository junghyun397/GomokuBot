package route

import database.DatabaseConnection
import inference.B3nzeneConnection
import session.SessionRepository

data class BotContext(
    val databaseConnection: DatabaseConnection,
    val b3nzeneConnection: B3nzeneConnection,
    val sessionRepository: SessionRepository,
)
