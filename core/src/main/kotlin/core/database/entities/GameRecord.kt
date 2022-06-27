package core.database.entities

import core.assets.UserUid
import core.session.GameResult
import jrenju.notation.Pos

data class GameRecord(
    val boardStatus: ByteArray,
    val history: List<Pos>?,

    val gameResult: GameResult,

    val blackId: UserUid,
    val whiteId: UserUid,
) {

    override fun equals(other: Any?) =
        this === other || (javaClass == other?.javaClass && (other as GameRecord).let {
            this.boardStatus.contentHashCode() == other.boardStatus.contentHashCode()
                    && this.blackId == other.blackId
                    && this.whiteId == other.whiteId
        })

    override fun hashCode(): Int {
        var result = boardStatus.contentHashCode()
        result = 31 * result + blackId.hashCode()
        result = 31 * result + whiteId.hashCode()
        return result
    }

}
