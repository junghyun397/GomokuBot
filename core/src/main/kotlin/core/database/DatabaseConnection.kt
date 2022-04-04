package core.database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitSingle
import reactor.core.publisher.Mono

@JvmInline
value class DatabaseConnection(val connection: Connection) {
    companion object {

        suspend fun connectionFrom(serverURL: String): DatabaseConnection =
            DatabaseConnection(Mono.from(ConnectionFactories.get(serverURL).create()).awaitSingle())

    }
}
