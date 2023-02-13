package core

import core.database.DatabaseConnection
import core.inference.ResRenjuClient
import core.session.SessionRepository

data class BotContext(
    val config: BotConfig,
    val dbConnection: DatabaseConnection,
    val resRenjuClient: ResRenjuClient,
    val sessions: SessionRepository,
)
