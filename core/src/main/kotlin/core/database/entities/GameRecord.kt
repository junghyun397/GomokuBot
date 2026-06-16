package core.database.entities

import core.assets.ChannelUid
import core.assets.User
import core.engine.EngineLevel
import core.session.entities.Rule
import renju.History
import renju.notation.ColorContainer
import renju.notation.GameResult
import kotlin.time.Instant

data class GameRecord(
    val gameRecordId: GameRecordId?,
    val channelId: ChannelUid,
    val users: ColorContainer<User>,

    val rule: Rule,
    val history: History,
    val gameResult: GameResult,
    val engineLevel: EngineLevel?,
    val ratingDelta: Float? = null,

    val date: Instant
)

@JvmInline value class GameRecordId(val id: Long)
