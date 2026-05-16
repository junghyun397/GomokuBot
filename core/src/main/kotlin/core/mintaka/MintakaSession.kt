package core.mintaka

import core.mintaka.types.ResponseSchema
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

    fun begins(begins: ResponseSchema.Begins): MintakaStreamingSession {
        return MintakaStreamingSession(
            sid=this.sid,
            token=this.token,
            hash=this.hash,
            lastResponse= ResponseSchema.Begins(begins.content)
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

    fun status(status: ResponseSchema.Status): MintakaStreamingSession {
        return this.copy(
            lastResponse=ResponseSchema.Status(status.content)
        )
    }

    fun bestmove(): MintakaIdleSession {
        return MintakaIdleSession(
            sid=this.sid,
            token=this.token,
            hash=this.hash
        )
    }

}
