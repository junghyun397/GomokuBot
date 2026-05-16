package core.session.entities

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.assets.User
import core.assets.aiUser
import core.mintaka.MintakaSession
import core.session.Rule
import renju.GameState
import renju.notation.GameResult

sealed interface RenjuSession : GameSession {

    fun next(state: GameState, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey): RenjuSession

}

data class AiGameSession(
    val mintakaSession: MintakaSession,
    override val owner: User,
    override val ownerHasBlack: Boolean,
    override val state: GameState,
    override val gameResult: Option<GameResult> = None,
    override val messageBufferKey: MessageBufferKey,
    override val recording: Boolean,
    override val ruleKind: Rule,
    override val expireService: ExpireService
) : RenjuSession {

    override val opponent = aiUser

    override fun next(state: GameState, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey) =
        this.copy(
            state = state,
            gameResult = gameResult,
            expireService = this.expireService.next(),
            messageBufferKey = messageBufferKey
        )

    override fun updateResult(gameResult: GameResult) = this.copy(gameResult = Some(gameResult))

}

data class PvpGameSession(
    override val owner: User,
    override val opponent: User,
    override val ownerHasBlack: Boolean,
    override val state: GameState,
    override val gameResult: Option<GameResult> = None,
    override val messageBufferKey: MessageBufferKey,
    override val recording: Boolean,
    override val ruleKind: Rule,
    override val expireService: ExpireService
) : RenjuSession {

    override fun next(state: GameState, gameResult: Option<GameResult>, messageBufferKey: MessageBufferKey) =
        this.copy(
            state = state,
            gameResult = gameResult,
            expireService = this.expireService.next(),
            messageBufferKey = messageBufferKey
        )

    override fun updateResult(gameResult: GameResult) = this.copy(gameResult = Some(gameResult))

}
