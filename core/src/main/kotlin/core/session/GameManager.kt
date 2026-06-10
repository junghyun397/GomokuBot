package core.session

import core.BotContext
import core.assets.Channel
import core.assets.User
import core.assets.humanId
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.mintaka.EngineLevel
import core.interact.message.graphics.*
import core.session.entities.MintakaIdleSession
import core.mintaka.MintakaProvider
import core.mintaka.MintakaServer
import core.session.entities.*
import renju.Board
import renju.GameState
import renju.History
import renju.MoveError
import renju.notation.Color
import renju.notation.GameResult
import renju.notation.HashKey
import renju.notation.Pos
import utils.lang.tuple
import utils.structs.Identifiable
import kotlin.random.Random

enum class BoardStyle(override val id: Short, val renderer: BoardRenderer, val sample: BoardRendererSample) : Identifiable {
    IMAGE(0, ImageBoardRenderer, ImageBoardRenderer),
    TEXT(1, TextBoardRenderer(), TextBoardRenderer),
    DOTTED_TEXT(2, DottedTextBoardRenderer(), DottedTextBoardRenderer),
}

enum class FocusType(override val id: Short) : Identifiable {
    INTELLIGENCE(0), CENTER(1)
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

    fun generatePvpSession(bot: BotContext, owner: User.Human, opponent: User.Human, rule: Rule): GameSession {
        val ownerStartsBlack = Random(System.nanoTime()).nextBoolean()
        val blackPlayer = if (ownerStartsBlack) owner else opponent
        val whitePlayer = if (ownerStartsBlack) opponent else owner
        val sessionId = SessionId.issue()

        return when (rule) {
            Rule.RENJU -> PvpGameSession(
                id = sessionId,
                blackPlayer = blackPlayer,
                whitePlayer = whitePlayer,
                state = GameState(Board.newBoard(), History.empty()),
                messageBufferKey = MessageBufferKey.issue(),
                recording = true,
                ruleKind = rule,
                expireService = ExpireService(bot.config.gameExpireAfter),
            )
            Rule.TARAGUCHI_10 -> TaraguchiSwapStageSession(
                id = sessionId,
                blackPlayer = blackPlayer,
                whitePlayer = whitePlayer,
                state = GameState(Board.newBoard(), History.empty()).play(Pos.CENTER),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireAfter),
                isBranched = false
            )
            Rule.SOOSYRV_8 -> SoosyrvMoveStageSession(
                id = sessionId,
                blackPlayer = blackPlayer,
                whitePlayer = whitePlayer,
                state = GameState(Board.newBoard(), History.empty()).play(Pos.CENTER),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireAfter),
                isBranched = false
            )
        }
    }

    suspend fun generateEngineSession(bot: BotContext, owner: User.Human, engineLevel: EngineLevel): GameSession {
        val humanHasBlack = Random(System.nanoTime()).nextBoolean()

        val state = if (humanHasBlack)
            GameState(Board.newBoard(), History.empty())
        else
            GameState(Board.newBoard(), History.empty()).play(Pos.CENTER)

        // TODO: PoC
        val sessionHandle = MintakaProvider.createSession(bot.mintakaServer, engineLevel, state)

        return EngineGameSession(
            id = SessionId.issue(),
            mintakaSession = sessionHandle.session.await(),
            humanPlayer = owner,
            blackPlayer = if (humanHasBlack) owner else User.GomokuBot,
            whitePlayer = if (humanHasBlack) User.GomokuBot else owner,
            state = state,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.RENJU,
            expireService = ExpireService(bot.config.gameExpireAfter),
        )
    }

    fun validateMove(session: GameSession, move: Pos): MoveError? =
        session.board.validateMove(move)
            ?: if (session is OpeningSession && !session.validateMove(move)) MoveError.Forbidden
            else null

    fun makeMove(session: RenjuSession, pos: Pos): RenjuSession {
        val thenState = session.state.play(pos)
        val result = thenState.board.winner()

        return if (result == null)
            session.next(thenState, null, MessageBufferKey.issue())
        else {
            val gameResult = when (result) {
                is GameResult.FiveInRow ->
                    GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), session.player, session.nextPlayer)
                else -> GameResult.Full
            }

            session.next(thenState, gameResult, session.messageBufferKey)
        }
    }

    suspend fun makeAiMove(server: MintakaServer, session: EngineGameSession, beforeHash: HashKey, playerMove: Pos): EngineGameSession {
        // TODO: PoC
        MintakaProvider.playSession(server, session.mintakaSession as MintakaIdleSession, beforeHash, playerMove)

        val launchHandle = MintakaProvider.launchSession(server, session.mintakaSession, session.state.board.hashKey)

        val aiMove = Pos.fromCartesian(launchHandle.bestmove.await().best_move)!!

        MintakaProvider.playSession(server, session.mintakaSession, session.state.board.hashKey, aiMove)
        // TODO: PoC

        val thenState = session.state.play(aiMove)
        val result = thenState.board.winner()

        return if (result == null)
            session.copy(
                state = thenState,
            )
        else {
            val gameResult = when (result) {
                is GameResult.FiveInRow ->
                    GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), User.GomokuBot, session.humanPlayer)
                else -> GameResult.Full
            }

            session.copy(
                state = thenState,
                gameResult = gameResult,
            )
        }
    }

    fun resignSession(session: GameSession, cause: GameResult.Cause, user: User): Pair<RenjuSession, GameResult.Win> =
        when (session) {
            is EngineGameSession -> {
                val winColor =
                    if (session.history.moves == 0)
                        if (session.blackPlayer == session.humanPlayer) Color.White
                        else Color.Black
                    else session.board.opponentColor

                val result = GameResult.Win(cause, winColor, User.GomokuBot, session.humanPlayer)

                tuple(session.copy(gameResult = result), result)
            }
            is OpeningSession, is PvpGameSession -> {
                val userId = user.humanId ?: throw IllegalStateException()

                val loser = when (userId) {
                    session.blackPlayer.humanId -> session.blackPlayer
                    session.whitePlayer.humanId -> session.whitePlayer
                    else -> throw IllegalStateException()
                }

                val winner =
                    if (loser == session.blackPlayer) session.whitePlayer
                    else session.blackPlayer

                val winColor =
                    if (winner == session.blackPlayer) Color.Black
                    else Color.White

                val result = GameResult.Win(cause, winColor, winner, loser)

                tuple(session.updateResult(result), result)
            }
        }

    suspend fun finishSession(bot: BotContext, channel: Channel, session: GameSession, result: GameResult) {
        SessionManager.deleteGameSession(bot.sessions, session.id)

        session.extractGameRecord(channel.id)?.let { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }
    }

}
