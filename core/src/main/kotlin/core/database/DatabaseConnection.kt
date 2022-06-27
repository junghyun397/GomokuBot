package core.database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitSingle
import reactor.kotlin.core.publisher.toMono

data class DatabaseConnection(
    val connection: Connection,
    val caches: Caches,
) {

    fun liftConnection() = this.connection.toMono()

    companion object {
        suspend fun connectionFrom(url: String, caches: Caches): DatabaseConnection =
            DatabaseConnection(
                ConnectionFactories.get(url)
                    .create()
                    .awaitSingle(),
                caches
            )
    }

}
