package core

import core.inference.KvineClient
import core.session.SessionRepository

data class BotContext(
    val config: BotConfig,
    val databaseConnection: core.database.DatabaseConnection,
    val kvineClient: KvineClient,
    val sessions: SessionRepository,
)
