package core.database.entities

import core.assets.GuildUid
import core.assets.UserUid
import core.assets.aiUser
import core.inference.AiLevel
import core.session.GameResult
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import jrenju.notation.Pos
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

fun GameSession.extractGameRecord(guildUid: GuildUid) =
    when {
        this.gameResult.isEmpty || !this.recording || this.history.any { it == null } -> Option.Empty
        else -> Option(GameRecord(
            boardStatus = board.boardField(),
            history = history.map { it ?: INVALID_POS },

            gameResult = gameResult.getOrException(),

            guildId = guildUid,
            blackId = when {
                ownerHasBlack -> owner
                else -> opponent
            }.let { if (it.id == aiUser.id) null else it.id },
            whiteId = when {
                ownerHasBlack -> opponent
                else -> owner
            }.let { if (it.id == aiUser.id) null else it.id },

            aiLevel = when (this) {
                is AiGameSession -> this.aiLevel
                else -> null
            },

            date = LinuxTime()
        ))
    }
