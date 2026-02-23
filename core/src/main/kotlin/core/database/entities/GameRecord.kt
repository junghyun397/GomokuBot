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
import core.inference.AiLevel
import core.session.Rule
import core.session.entities.*
import renju.Board
import renju.notation.GameResult
import renju.notation.Pos
import utils.assets.LinuxTime

@Suppress("ArrayInDataClass")
data class GameRecord(
    val gameRecordId: Option<GameRecordId>,

    val boardState: ByteArray,
    val history: List<Pos>,

    val gameResult: GameResult,

    val channelId: ChannelUid,
    val blackId: UserUid?,
    val whiteId: UserUid?,

    val aiLevel: AiLevel?,

    val rule: Rule,

    val date: LinuxTime
)

@JvmInline value class GameRecordId(val id: Long)

private val invalidPos: Pos = Pos.fromIdx(-1)
fun GameSession.extractGameRecord(channelUid: ChannelUid): Option<GameRecord> =
    if (this.gameResult.isSome() && this.recording && !this.history.contains(null))
        Some(
            GameRecord(
                gameRecordId = None,

                boardState = board.field,
                history = history.map { it ?: invalidPos },

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
                    is AiGameSession -> this.aiLevel
                    else -> null
                },

                rule = ruleKind,

                date = LinuxTime.now()
            )
        )
    else None

suspend fun GameRecord.asGameSession(dbConnection: DatabaseConnection, owner: User): GameSession {
    val board = Board.fromFieldArray(boardState, history.last().idx()).getOrNull()
        ?: error("invalid game record board data")

    return when {
        whiteId != null && blackId != null -> {
            val ownerHasBlack = blackId == owner.id

            PvpGameSession(
                owner = owner,
                opponent = if (ownerHasBlack)
                    UserProfileRepository.retrieveUser(dbConnection, whiteId)
                else
                    UserProfileRepository.retrieveUser(dbConnection, blackId),
                ownerHasBlack = ownerHasBlack,
                board = board,
                gameResult = Some(gameResult),
                history = history,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(0, date, date),
                ruleKind = rule,
                recording = false,
            )
        }
        else -> {
            val ownerHasBlack = whiteId == null

            AiGameSession(
                aiLevel = aiLevel!!,
                solution = None,
                owner = owner,
                ownerHasBlack = ownerHasBlack,
                board = board,
                gameResult = Some(gameResult),
                history = history,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(0, date, date),
                ruleKind = rule,
                recording = false,
            )
        }
    }
}
