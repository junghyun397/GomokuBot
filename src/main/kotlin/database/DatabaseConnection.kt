package database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitSingle

data class DatabaseConnection(val connection: Connection) {
    companion object {

        suspend fun connectionFrom(serverURL: String): DatabaseConnection =
            DatabaseConnection(
                connection = ConnectionFactories
                    .get(serverURL)
                    .create()
                    .awaitSingle()
            )

    }
}
