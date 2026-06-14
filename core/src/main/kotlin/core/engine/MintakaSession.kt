package core.engine

import core.engine.types.Response
import renju.notation.HashKey
import renju.notation.Pos
import kotlin.time.Instant

data class BestMove(
    val move: Pos?,
    val winRate: Float,
    val hash: HashKey,
)

fun core.engine.types.BestMove.asBestMove(): BestMove =
    BestMove(
        move = this.best_move?.let { Pos.fromCartesian(it) },
        winRate = 0.0f,
        hash = HashKey.from(this.position_hash)!!
    )

data class Status(
    val bestMove: Pos?,
    val winRate: Float,
)

fun Response.Status.asStatus(): Status =
    Status(
        bestMove = this.content.best_move?.let { Pos.fromCartesian(it) },
        winRate = 0.0f
    )

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
        return this.copy(hash = hash)
    }

    fun launch(): MintakaLaunchingSession {
        return MintakaLaunchingSession(
            sid = this.sid,
            token = this.token,
            hash = this.hash,
        )
    }

}

data class MintakaLaunchingSession(
    override val sid: String,
    override val token: String,
    override val hash: HashKey,
    val waitingState: WaitingState? = null,
) : MintakaSession {

    fun begins(): MintakaStreamingSession {
        return MintakaStreamingSession(
            sid = this.sid,
            token = this.token,
            hash = this.hash,
        )
    }

}

data class MintakaStreamingSession(
    override val sid: String,
    override val token: String,
    override val hash: HashKey,
    val status: Status? = null,
    val aborted: Boolean = false,
) : MintakaSession {

    fun status(status: Status): MintakaStreamingSession {
        return this.copy(
            status = status
        )
    }

    fun bestMove(): MintakaIdleSession {
        return MintakaIdleSession(
            sid = this.sid,
            token = this.token,
            hash = this.hash
        )
    }

}
