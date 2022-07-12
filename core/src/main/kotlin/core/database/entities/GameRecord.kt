package core.database.entities

import core.assets.GuildUid
import core.assets.UserUid
import core.inference.AiLevel
import core.session.GameResult
import jrenju.notation.Pos
import utils.assets.LinuxTime

@Suppress("ArrayInDataClass")
data class GameRecord(
    val boardStatus: ByteArray,
    val history: List<Pos>?,

    val gameResult: GameResult,

    val guildId: GuildUid,
    val blackId: UserUid?,
    val whiteId: UserUid?,

    val aiLevel: AiLevel?,

    val date: LinuxTime
)
