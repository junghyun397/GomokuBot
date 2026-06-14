package core.database.entities

import core.assets.ChannelUid
import core.assets.UserUid
import core.engine.EngineLevel
import core.session.entities.EngineGameSession
import core.session.entities.GameSession
import core.session.entities.Rule
import renju.notation.ColorContainer
import renju.notation.GameResult
import renju.notation.Pos
import kotlin.time.Clock
import kotlin.time.Instant

data class GameRecord(
    val gameRecordId: GameRecordId?,
    val channelId: ChannelUid,
    val userUid: ColorContainer<UserUid?>,

    val rule: Rule,
    val history: List<Pos>,
    val gameResult: GameResult,
    val engineLevel: EngineLevel?,

    val date: Instant
)

@JvmInline value class GameRecordId(val id: Long)

fun GameSession.extractGameRecord(channelUid: ChannelUid): GameRecord? =
    if (this.gameResult != null && this.recording && null !in this.state.history)
        GameRecord(
            gameRecordId = null,
            channelId = channelUid,
            userUid = this.users.map { it.id },

            rule = this.ruleKind,
            history = this.state.history.sequence.filterNotNull(),
            gameResult = this.gameResult!!,
            engineLevel = when (this) {
                is EngineGameSession -> this.engineLevel
                else -> null
            },

            date = Clock.System.now()
        )
    else null
