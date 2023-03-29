package core.session.entities

import core.assets.User
import core.session.Rule
import renju.Board
import renju.notation.Pos
import utils.assets.LinuxTime
import utils.structs.Option

sealed interface GameSession : Expirable {

    val expireService: ExpireService

    override val expireDate: LinuxTime get() = this.expireService.expireAt

    val owner: User
    val opponent: User
    val ownerHasBlack: Boolean

    val board: Board

    val gameResult: Option<GameResult>

    val history: List<Pos?>

    val messageBufferKey: MessageBufferKey

    val recording: Boolean

    val ruleKind: Rule

    val player get() = when (this.ownerHasBlack) {
        this.board.isNextColorBlack -> this.owner
        else -> this.opponent
    }

    val nextPlayer get() = when (this.ownerHasBlack) {
        this.board.isNextColorBlack -> this.opponent
        else -> this.owner
    }

    fun updateResult(gameResult: GameResult): RenjuSession

}
