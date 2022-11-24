package core.database

import io.r2dbc.spi.Connection
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

data class DatabaseConnection(
    val connection: Connection,
    val localCaches: LocalCaches,
) {

    fun liftConnection(): Mono<Connection> = this.connection.toMono()

}
