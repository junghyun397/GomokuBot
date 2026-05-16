package core.session

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.assets.aiUser
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.mintaka.AiLevel
import core.interact.message.graphics.*
import core.mintaka.MintakaIdleSession
import core.mintaka.MintakaProvider
import core.mintaka.MintakaServer
import core.session.entities.*
import renju.Board
import renju.GameState
import renju.History
import renju.MoveError
import renju.notation.Color
import renju.notation.GameResult
import renju.notation.Pos
import utils.lang.tuple
import utils.structs.Identifiable
import kotlin.random.Random

enum class BoardStyle(override val id: Short, val renderer: BoardRenderer, val sample: BoardRendererSample) : Identifiable {
    IMAGE(0, ImageBoardRenderer, ImageBoardRenderer),
    TEXT(1, TextBoardRenderer(), TextBoardRenderer),
    DOTTED_TEXT(2, DottedTextBoardRenderer(), DottedTextBoardRenderer),
    UNICODE(3, UnicodeBoardRenderer, UnicodeBoardRenderer)
}

enum class FocusType(override val id: Short) : Identifiable {
    INTELLIGENCE(0), FALLOWING(1)
}

enum class HintType(override val id: Short) : Identifiable {
    OFF(0), FIVE(1)
}

enum class SwapType(override val id: Short) : Identifiable {
    RELAY(0), ARCHIVE(1), EDIT(2)
}

enum class ArchivePolicy(override val id: Short) : Identifiable {
    WITH_PROFILE(0), BY_ANONYMOUS(1), PRIVACY(2)
}

enum class Rule(override val id: Short, val isOpening: Boolean) : Identifiable {
    RENJU(0, false), TARAGUCHI_10(1, true), SOOSYRV_8(2, true)
}

object GameManager {

    fun generatePvpSession(bot: BotContext, owner: User, opponent: User, rule: Rule): GameSession {
        val ownerHasBlack = Random(System.nanoTime()).nextBoolean()

        return when (rule) {
            Rule.RENJU -> PvpGameSession(
                owner = owner,
                opponent = opponent,
                ownerHasBlack = ownerHasBlack,
                state = GameState(Board.newBoard(), History.empty()),
                messageBufferKey = MessageBufferKey.issue(),
                recording = true,
                ruleKind = rule,
                expireService = ExpireService(bot.config.gameExpireAfter),
            )
            Rule.TARAGUCHI_10 -> TaraguchiSwapStageSession(
                owner = owner,
                opponent = opponent,
                ownerHasBlack = ownerHasBlack,
                state = GameState(Board.newBoard(), History.empty()).play(Pos.CENTER),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireAfter),
                isBranched = false
            )
            Rule.SOOSYRV_8 -> SoosyrvMoveStageSession(
                owner = owner,
                opponent = opponent,
                ownerHasBlack = ownerHasBlack,
                state = GameState(Board.newBoard(), History.empty()).play(Pos.CENTER),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireAfter),
                isBranched = false
            )
        }
    }

    suspend fun generateAiSession(bot: BotContext, owner: User, aiLevel: AiLevel): GameSession {
        val ownerHasBlack = Random(System.nanoTime()).nextBoolean()

        val state = if (ownerHasBlack)
            GameState(Board.newBoard(), History.empty())
        else
            GameState(Board.newBoard(), History.empty()).play(Pos.CENTER)

        // TODO: PoC
        val sessionHandle = MintakaProvider.createSession(bot.mintakaServer, MintakaProvider.DefaultConfig, MintakaProvider.EmptyBoard)

        return AiGameSession(
            mintakaSession = sessionHandle.session.await(),
            owner = owner,
            ownerHasBlack = ownerHasBlack,
            state = state,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.RENJU,
            expireService = ExpireService(bot.config.gameExpireAfter),
        )
    }

    fun validateMove(session: GameSession, move: Pos): Option<MoveError> =
        session.board.validateMove(move)
            .fold(
                ifEmpty = {
                    if (session is OpeningSession && !session.validateMove(move))
                        Some(MoveError.Forbidden)
                    else
                        None
                },
                ifSome = { Some(it) }
            )

    fun makeMove(session: RenjuSession, pos: Pos): RenjuSession {
        val thenState = session.state.play(pos)

        return thenState.board.winner().fold(
            { session.next(thenState, None, MessageBufferKey.issue()) },
            { result ->
                val gameResult = when (result) {
                    is GameResult.FiveInRow ->
                        GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), session.player, session.nextPlayer)
                    else -> GameResult.Full
                }

                session.next(thenState, Some(gameResult), session.messageBufferKey)
            }
        )
    }

    suspend fun makeAiMove(server: MintakaServer, session: AiGameSession): AiGameSession {
        // TODO: PoC
        val launchHandle = MintakaProvider.launchSession(server, session.mintakaSession as MintakaIdleSession)

        val aiMove = Pos.fromCartesian(launchHandle.bestmove.await().best_move)!!

        val thenState = session.state.play(aiMove)

        return thenState.board.winner().fold(
            {
                session.copy(
                    state = thenState,
                )
            },
            { result ->
                val gameResult = when (result) {
                    is GameResult.FiveInRow ->
                        GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), aiUser, session.owner)
                    else -> GameResult.Full
                }

                session.copy(
                    state = thenState,
                    gameResult = Some(gameResult),
                )
            }
        )
    }

    fun resignSession(session: GameSession, cause: GameResult.Cause, user: User): Pair<RenjuSession, GameResult.Win> =
        when (session) {
            is AiGameSession -> {
                val winColor =
                    if (session.board.stones == 0)
                        if (session.ownerHasBlack) Color.White
                        else Color.Black
                    else session.board.opponentColor

                val result = GameResult.Win(cause, winColor, aiUser, session.owner)

                tuple(session.copy(gameResult = Some(result)), result)
            }
            is OpeningSession, is PvpGameSession -> {
                val winColor =
                    if ((session.player.id == user.id) == session.ownerHasBlack) Color.White
                    else Color.Black

                val result =
                    if (user.id == session.owner.id)
                        GameResult.Win(cause, winColor, session.opponent, session.owner)
                    else
                        GameResult.Win(cause, winColor, session.owner, session.opponent)

                tuple(session.updateResult(result), result)
            }
        }

    suspend fun finishSession(bot: BotContext, guild: Channel, session: GameSession, result: GameResult) {
        SessionManager.removeGameSession(bot.sessions, guild, session.owner.id)

        session.extractGameRecord(guild.id).onSome { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }
    }

}
