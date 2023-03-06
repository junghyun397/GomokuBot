package core

import core.database.DatabaseConnection
import core.inference.ResRenjuClient
import core.session.SessionPool

data class BotContext(
    val config: BotConfig,
    val dbConnection: DatabaseConnection,
    val resRenjuClient: ResRenjuClient,
    val sessions: SessionPool,
)
