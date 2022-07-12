package core.database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitFirst
import reactor.kotlin.core.publisher.toMono

data class DatabaseConnection(
    val connection: Connection,
    val localCaches: LocalCaches,
) {

    fun liftConnection() = this.connection.toMono()

}
