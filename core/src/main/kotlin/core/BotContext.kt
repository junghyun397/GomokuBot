package core

import core.database.DatabaseConnection
import core.inference.KvineClient
import core.session.SessionRepository

data class BotContext(
    val config: BotConfig,
    val dbConnection: DatabaseConnection,
    val kvineClient: KvineClient,
    val sessions: SessionRepository,
)
