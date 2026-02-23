package core

import core.database.DatabaseConnection
import core.session.SessionPool

data class BotContext(
    val config: BotConfig,
    val dbConnection: DatabaseConnection,
    val sessions: SessionPool,
)
