package core.database.entities

import core.assets.ChannelUid
import core.assets.UserUid
import core.assets.humanId
import core.mintaka.EngineLevel
import core.session.Rule
import core.session.entities.EngineGameSession
import core.session.entities.GameSession
import renju.notation.GameResult
import renju.notation.Pos
import kotlin.time.Clock
import kotlin.time.Instant

data class GameRecord(
    val gameRecordId: GameRecordId?,

    val history: List<Pos>,

    val gameResult: GameResult,

    val channelId: ChannelUid,
    val blackId: UserUid?,
    val whiteId: UserUid?,

    val engineLevel: EngineLevel?,

    val rule: Rule,

    val date: Instant
)

@JvmInline value class GameRecordId(val id: Long)

fun GameSession.extractGameRecord(channelUid: ChannelUid): GameRecord? =
    if (this.gameResult != null && this.recording && null !in this.history)
        GameRecord(
            gameRecordId = null,

            history = this.history.sequence.filterNotNull(),

            gameResult = this.gameResult!!,

            channelId = channelUid,
            blackId = this.blackPlayer.humanId,
            whiteId = this.whitePlayer.humanId,

            engineLevel = when (this) {
                is EngineGameSession -> EngineLevel.AMOEBA
                else -> null
            },

            rule = this.ruleKind,

            date = Clock.System.now()
        )
    else null
