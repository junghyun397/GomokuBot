package core.database.entities

import core.assets.GuildUid
import core.assets.UserUid
import core.assets.aiUser
import core.inference.AiLevel
import core.session.GameResult
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import renju.notation.Pos
import utils.assets.LinuxTime
import utils.structs.Option
import utils.structs.getOrException

@Suppress("ArrayInDataClass")
data class GameRecord(
    val boardStatus: ByteArray,
    val history: List<Pos>,

    val gameResult: GameResult,

    val guildId: GuildUid,
    val blackId: UserUid?,
    val whiteId: UserUid?,

    val aiLevel: AiLevel?,

    val date: LinuxTime
)

private val INVALID_POS: Pos = Pos.fromIdx(-1)

fun GameSession.extractGameRecord(guildUid: GuildUid): Option<GameRecord> =
    Option.cond(this.gameResult.isDefined && this.recording && !this.history.contains(null)) {
        GameRecord(
            boardStatus = board.field(),
            history = history.map { it ?: INVALID_POS },

            gameResult = gameResult.getOrException(),

            guildId = guildUid,
            blackId = when {
                ownerHasBlack -> owner.id
                else -> opponent.id
            }.takeIf { it != aiUser.id },
            whiteId = when {
                ownerHasBlack -> opponent.id
                else -> owner.id
            }.takeIf { it != aiUser.id },

            aiLevel = when (this) {
                is AiGameSession -> this.aiLevel
                else -> null
            },

            date = LinuxTime.now()
        )
    }
