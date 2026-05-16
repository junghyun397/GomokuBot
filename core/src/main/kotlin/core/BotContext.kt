package core

import core.database.DatabaseConnection
import core.mintaka.MintakaServer
import core.session.SessionPool

data class BotContext(
    val config: BotConfig,
    val dbConnection: DatabaseConnection,
    val mintakaServer: MintakaServer,
    val sessions: SessionPool,
)
