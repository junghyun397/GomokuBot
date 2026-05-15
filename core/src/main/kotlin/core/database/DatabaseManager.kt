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

    suspend fun initCaches(connection: DatabaseConnection) {
        connection.localCaches.announceCache = AnnounceRepository.fetchAnnounces(connection)
    }

    internal fun smallIntToByte(smallInt: Any?): Byte = (smallInt as Short).toByte()

    internal fun smallIntToMaybeByte(smallInt: Any?): Byte? = (smallInt as? Short)?.toByte()

}
