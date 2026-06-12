package core.session

import core.BotContext
import core.assets.Channel
import core.assets.User
import core.assets.humanId
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.mintaka.EngineLevel
import core.mintaka.MintakaProvider
import core.mintaka.MintakaServer
import core.session.entities.*
import renju.Board
import renju.GameState
import renju.History
import renju.MoveError
import renju.notation.*
import utils.lang.tuple
import kotlin.random.Random

object GameManager {

    fun generatePvpSession(bot: BotContext, requester: User.Human, opponent: User.Human, rule: Rule): GameSession {
        val requesterStartsBlack = Random(System.nanoTime()).nextBoolean()
        val blackPlayer = if (requesterStartsBlack) requester else opponent
        val whitePlayer = if (requesterStartsBlack) opponent else requester
        val user = ColorContainer(blackPlayer, whitePlayer)
        val sessionId = SessionId.issue()
        fun newContext(state: GameState) = GameSessionContext(
            id = sessionId,
            user = user,
            state = state,
            messageBufferKey = MessageBufferKey.issue(),
            expireService = ExpireService(bot.config.gameExpireAfter),
        )

        return when (rule) {
            Rule.RENJU -> PvpGameSession(
                context = newContext(GameState(Board.newBoard(), History.empty())),
                recording = true,
                ruleKind = rule,
            )
            Rule.TARAGUCHI_10 -> TaraguchiSwapStageSession(
                context = newContext(GameState(Board.newBoard(), History.empty()).play(Pos.CENTER)),
                isBranched = false
            )
            Rule.SOOSYRV_8 -> SoosyrvMoveStageSession(
                context = newContext(GameState(Board.newBoard(), History.empty()).play(Pos.CENTER)),
                isBranched = false
            )
            else -> throw NotImplementedError()
        }
    }

    suspend fun generateEngineSession(bot: BotContext, humanPlayer: User.Human, engineLevel: EngineLevel): GameSession {
        val humanHasBlack = Random(System.nanoTime()).nextBoolean()

        val state = if (humanHasBlack)
            GameState(Board.newBoard(), History.empty())
        else
            GameState(Board.newBoard(), History.empty()).play(Pos.CENTER)

        // TODO: PoC
        val sessionHandle = MintakaProvider.createSession(bot.mintakaServer, engineLevel, state)

        return EngineGameSession(
            context = GameSessionContext(
                id = SessionId.issue(),
                user = ColorContainer(if (humanHasBlack) humanPlayer else User.GomokuBot, if (humanHasBlack) User.GomokuBot else humanPlayer),
                state = state,
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireAfter),
            ),
            mintakaSession = sessionHandle.session.await(),
            humanPlayer = humanPlayer,
            recording = true,
            ruleKind = Rule.RENJU,
        )
    }

    fun validateMove(session: GameSession, move: Pos): MoveError? =
        session.state.board.validateMove(move)
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
                    GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), session.player, session.opponent)
                else -> GameResult.Full
            }

            session.next(thenState, gameResult, session.messageBufferKey)
        }
    }

    suspend fun makeEngineMove(server: MintakaServer, session: EngineGameSession, beforeHash: HashKey, playerMove: Pos): EngineGameSession {
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
                context = session.context.copy(state = thenState),
            )
        else {
            val gameResult = when (result) {
                is GameResult.FiveInRow ->
                    GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), User.GomokuBot, session.humanPlayer)
                else -> GameResult.Full
            }

            session.copy(
                context = session.context.copy(state = thenState),
                gameResult = gameResult,
            )
        }
    }

    fun resignSession(session: GameSession, cause: GameResult.Cause, user: User): Pair<RenjuSession, GameResult.Win> =
        when (session) {
            is EngineGameSession -> {
                val winColor =
                    if (session.state.history.moves == 0)
                        if (session.user.black == session.humanPlayer) Color.White
                        else Color.Black
                    else !session.state.board.playerColor

                val result = GameResult.Win(cause, winColor, User.GomokuBot, session.humanPlayer)

                tuple(session.copy(gameResult = result), result)
            }
            is OpeningSession, is PvpGameSession -> {
                val winColor = !session.user.map { it.humanId }.color(user.humanId)!!

                val result = GameResult.Win(cause, winColor, session.user[winColor], session.user[!winColor])

                val session = when (session) {
                    is PvpGameSession -> session.copy(gameResult = result)
                    is OpeningSession -> session.asFinishedPvpSession(result)
                    else -> throw IllegalStateException("unreachable")
                }

                tuple(session, result)
            }
        }

    suspend fun finishSession(bot: BotContext, channel: Channel, session: GameSession) {
        SessionManager.deleteGameSession(bot.sessions, session.id)

        session.extractGameRecord(channel.id)?.let { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }
    }

}
