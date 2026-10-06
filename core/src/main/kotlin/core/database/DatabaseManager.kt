package core.database

import core.database.repositories.AnnounceRepository
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitFirst

object DatabaseManager {

    suspend fun newConnectionFrom(url: String, localCaches: LocalCaches): DatabaseConnection =
        DatabaseConnection(
            ConnectionFactories.get(url)
                .create()
                .awaitFirst(),
            localCaches
        )

    context(connection: DatabaseConnection)
    suspend fun initCaches() {
        connection.localCaches.announceCache = AnnounceRepository.fetchAnnounces()
    }

}
