package core

import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.session.SessionPool

data class BotContext(
    val dbConnection: DatabaseConnection,
    val mintakaServer: MintakaServer,
    val sessions: SessionPool,
)
