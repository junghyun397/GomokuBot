package core.database.entities

import core.assets.*
import core.database.repositories.UserProfileRepository
import core.inference.AiLevel
import core.inference.Token
import core.session.Rule
import core.session.SessionPool
import core.session.entities.*
import renju.notation.Pos
import utils.assets.LinuxTime
import utils.structs.Option
import utils.structs.getOrException

@Suppress("ArrayInDataClass")
data class GameRecord(
    val gameRecordId: Option<GameRecordId>,

    val boardState: ByteArray,
    val history: List<Pos>,

    val gameResult: GameResult,

    val guildId: GuildUid,
    val blackId: UserUid?,
    val whiteId: UserUid?,

    val aiLevel: AiLevel?,

    val rule: Rule,

    val date: LinuxTime
)

@JvmInline value class GameRecordId(val id: Long)

private val invalidPos: Pos = Pos.fromIdx(-1)

fun GameSession.extractGameRecord(guildUid: GuildUid): Option<GameRecord> =
    Option.cond(this.gameResult.isDefined && this.recording && !this.history.contains(null)) {
        GameRecord(
            gameRecordId = Option.Empty,

            boardState = board.field(),
            history = history.map { it ?: invalidPos },

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

            rule = ruleKind,

            date = LinuxTime.now()
        )
    }

suspend fun GameRecord.asGameSession(pool: SessionPool, owner: User): GameSession =
    when {
        whiteId != null && blackId != null -> {
            val ownerHasBlack = blackId == owner.id

            val board = Notation.BoardIOInstance.fromFieldArray(boardState, history.last().idx()).get()

            PvpGameSession(
                owner = owner,
                opponent = if (ownerHasBlack)
                    UserProfileRepository.retrieveUser(pool.dbConnection, whiteId)
                else
                    UserProfileRepository.retrieveUser(pool.dbConnection, blackId),
                ownerHasBlack = ownerHasBlack,
                board = board,
                gameResult = Option.Some(gameResult),
                history = history,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(0, date, date),
                ruleKind = rule,
                recording = false,
            )
        }
        else -> {
            val ownerHasBlack = whiteId == null

            val board = Notation.BoardIOInstance.fromPosSequence(history.joinToString(separator = "")).get()

            AiGameSession(
                aiLevel = aiLevel!!,
                solution = Option.Empty,
                resRenjuToken = Token(""),
                owner = owner,
                ownerHasBlack = ownerHasBlack,
                board = board,
                gameResult = Option.Some(gameResult),
                history = history,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(0, date, date),
                ruleKind = rule,
                recording = false,
            )
        }
    }
