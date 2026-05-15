package core.mintaka

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow

data class MintakaServer(
    val url: String,
    val password: String,
)

object MintakaProvider {

    data class CreateSessionHandle(
        val waiting: StateFlow<WaitingState?>,
        val session: Deferred<MintakaIdleSession>,
    )

    fun createSession(server: MintakaServer): CreateSessionHandle {
        throw NotImplementedError()
    }

    suspend fun commandSession(server: MintakaServer): CommandResult {
        throw NotImplementedError()
    }

    data class PlaySessionHandle(
        val waiting: StateFlow<WaitingState?>,
        val begins: Deferred<ResponseSchema.Begins>,
        val status: StateFlow<ResponseSchema.Status>,
        val bestmove: Deferred<BestMove>,
        val abort: () -> Unit,
    )

    fun playSession(server: MintakaServer, token: HashKey): PlaySessionHandle {
        throw NotImplementedError()
    }

    fun deleteSession(server: MintakaServer, token: HashKey) {
        throw NotImplementedError()
    }

}
