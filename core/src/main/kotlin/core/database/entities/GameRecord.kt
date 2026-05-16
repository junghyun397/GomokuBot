package core.database.entities

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.assets.ChannelUid
import core.assets.User
import core.assets.UserUid
import core.assets.aiUser
import core.database.DatabaseConnection
import core.database.repositories.UserProfileRepository
import core.mintaka.AiLevel
import core.session.Rule
import core.session.entities.*
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

    val aiLevel: AiLevel?,

    val rule: Rule,

    val date: Instant
)

@JvmInline value class GameRecordId(val id: Long)

fun GameSession.extractGameRecord(channelUid: ChannelUid): Option<GameRecord> =
    if (this.gameResult.isSome() && this.recording && null !in this.history)
        Some(
            GameRecord(
                gameRecordId = None,

                history = history.inner.filterNotNull(),

                gameResult = gameResult.getOrNull()!!,

                channelId = channelUid,
                blackId = when {
                    ownerHasBlack -> owner.id
                    else -> opponent.id
                }.takeIf { it != aiUser.id },
                whiteId = when {
                    ownerHasBlack -> opponent.id
                    else -> owner.id
                }.takeIf { it != aiUser.id },

                aiLevel = when (this) {
                    is AiGameSession -> AiLevel.AMOEBA
                    else -> null
                },

                rule = ruleKind,

                date = Clock.System.now()
            )
        )
    else None
