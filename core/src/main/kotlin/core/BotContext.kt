package core

import core.inference.B3nzeneClient
import core.session.SessionRepository

data class BotContext(
    val config: BotConfig,
    val databaseConnection: core.database.DatabaseConnection,
    val b3NzeneClient: B3nzeneClient,
    val sessionRepository: SessionRepository,
)
