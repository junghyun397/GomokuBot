package core.database.entities

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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
    val gameRecordId: Option<GameRecordId>,

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

fun GameSession.extractGameRecord(channelUid: ChannelUid): Option<GameRecord> =
    if (this.gameResult.isSome() && this.recording && null !in this.history)
        Some(
            GameRecord(
                gameRecordId = None,

                history = history.sequence.filterNotNull(),

                gameResult = gameResult.getOrNull()!!,

                channelId = channelUid,
                blackId = when {
                    ownerHasBlack -> owner.humanId
                    else -> opponent.humanId
                },
                whiteId = when {
                    ownerHasBlack -> opponent.humanId
                    else -> owner.humanId
                },

                engineLevel = when (this) {
                    is EngineGameSession -> EngineLevel.AMOEBA
                    else -> null
                },

                rule = ruleKind,

                date = Clock.System.now()
            )
        )
    else None
