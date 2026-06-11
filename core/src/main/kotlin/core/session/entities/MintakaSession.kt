package core.session.entities

import core.mintaka.MintakaResponse
import core.mintaka.types.Response
import renju.notation.HashKey
import kotlin.time.Instant

sealed interface WaitingState {

    data class Waiting(val since: Instant, val retryAfterMs: Long): WaitingState

    object Rejected: WaitingState

}

sealed interface MintakaSession {
    val sid: String
    val token: String
    val hash: HashKey
}

data class MintakaIdleSession(
    override val sid: String,
    override val token: String,
    override val hash: HashKey,
) : MintakaSession {

    fun command(hash: HashKey): MintakaIdleSession {
        return this.copy(hash=hash)
    }

    fun launch(): MintakaLaunchingSession {
        return MintakaLaunchingSession(
            sid=this.sid,
            token=this.token,
            hash=this.hash,
            waitingState = null
        )
    }

}

data class MintakaLaunchingSession(
    override val sid: String,
    override val token: String,
    override val hash: HashKey,
    val waitingState: WaitingState?,
) : MintakaSession {

    fun begins(begins: Response.Begins): MintakaStreamingSession {
        return MintakaStreamingSession(
            sid=this.sid,
            token=this.token,
            hash=this.hash,
            lastResponse= Response.Begins(begins.content)
        )
    }

}

data class MintakaStreamingSession(
    override val sid: String,
    override val token: String,
    override val hash: HashKey,
    val lastResponse: MintakaResponse,
    val aborted: Boolean = false,
) : MintakaSession {

    fun status(status: Response.Status): MintakaStreamingSession {
        return this.copy(
            lastResponse= Response.Status(status.content)
        )
    }

    fun bestMove(): MintakaIdleSession {
        return MintakaIdleSession(
            sid=this.sid,
            token=this.token,
            hash=this.hash
        )
    }

}
