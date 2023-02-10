package core.database.entities

import core.assets.*
import core.database.repositories.UserProfileRepository
import core.inference.AiLevel
import core.session.GameResult
import core.session.SessionRepository
import core.session.Token
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.MessageBufferKey
import core.session.entities.PvpGameSession
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

suspend fun GameRecord.asGameSession(repo: SessionRepository, owner: User): GameSession =
    when {
        whiteId != null && blackId != null -> {
            val ownerHasBlack = blackId == owner.id

            val board = Notation.BoardIOInstance.fromFieldArray(boardStatus, history.last().idx()).get()

            PvpGameSession(
                owner = owner,
                opponent = if (ownerHasBlack)
                    UserProfileRepository.retrieveUser(repo.dbConnection, whiteId)
                else UserProfileRepository.retrieveUser(repo.dbConnection, blackId),
                ownerHasBlack = ownerHasBlack,
                board = board,
                gameResult = Option.Some(gameResult),
                history = history,
                messageBufferKey = MessageBufferKey.issue(),
                expireOffset = 0,
                recording = false,
                expireDate = date,
                createDate = date,
            )
        }
        else -> {
            val ownerHasBlack = whiteId == null

            val board = Notation.BoardIOInstance.fromPosSequence(history.joinToString(separator = "")).get()

            AiGameSession(
                aiLevel = aiLevel!!,
                solution = Option.Empty,
                kvineToken = Token(""),
                owner = owner,
                ownerHasBlack = ownerHasBlack,
                board = board,
                gameResult = Option.Some(gameResult),
                history = history,
                messageBufferKey = MessageBufferKey.issue(),
                expireOffset = 0,
                recording = false,
                expireDate = date,
                createDate = date
            )
        }
    }
