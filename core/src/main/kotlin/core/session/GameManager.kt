package core.session

import core.BotContext
import core.assets.*
import core.database.entities.extractGameRecord
import core.database.repositories.GameRecordRepository
import core.inference.AiLevel
import core.inference.FocusSolver
import core.inference.ResRenjuClient
import core.inference.Token
import core.interact.message.graphics.*
import core.session.entities.*
import renju.ScalaBoard
import renju.`ScalaBoard$`
import renju.notation.InvalidKind
import renju.notation.Pos
import renju.notation.Renju
import renju.notation.Result
import renju.protocol.SolutionNode
import utils.lang.tuple
import utils.structs.*
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
                board = Notation.EmptyBoard,
                history = emptyList(),
                messageBufferKey = MessageBufferKey.issue(),
                recording = true,
                ruleKind = rule,
                expireService = ExpireService(bot.config.gameExpireOffset),
            )
            Rule.TARAGUCHI_10 -> TaraguchiSwapStageSession(
                owner = owner,
                opponent = opponent,
                ownerHasBlack = ownerHasBlack,
                board = `ScalaBoard$`.`MODULE$`.newBoard(),
                history = listOf(Renju.BOARD_CENTER_POS()),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireOffset),
                isBranched = false
            )
            Rule.SOOSYRV_8 -> SoosyrvMoveStageSession(
                owner = owner,
                opponent = opponent,
                ownerHasBlack = ownerHasBlack,
                board = `ScalaBoard$`.`MODULE$`.newBoard(),
                history = listOf(Renju.BOARD_CENTER_POS()),
                messageBufferKey = MessageBufferKey.issue(),
                expireService = ExpireService(bot.config.gameExpireOffset),
                isBranched = false
            )
        }
    }

    suspend fun generateAiSession(bot: BotContext, owner: User, aiLevel: AiLevel): GameSession {
        val ownerHasBlack = Random(System.nanoTime()).nextBoolean()

        val board = if (ownerHasBlack) Notation.EmptyBoard else ScalaBoard.newBoard()

        val history = if (ownerHasBlack) emptyList() else listOf(Renju.BOARD_CENTER_POS())

        val aiColor = if (ownerHasBlack) Notation.Color.White else Notation.Color.Black

        val token = when (aiLevel) {
            AiLevel.AMOEBA -> Token("AMOEBA")
            else -> bot.resRenjuClient.begins(aiLevel.aiPreset, aiColor, board)
        }

        return AiGameSession(
            owner = owner,
            aiLevel = aiLevel,
            resRenjuToken = token,
            solution = Option.Empty,
            ownerHasBlack = ownerHasBlack,
            board = board,
            history = history,
            messageBufferKey = MessageBufferKey.issue(),
            recording = true,
            ruleKind = Rule.RENJU,
            expireService = ExpireService(bot.config.gameExpireOffset),
        )
    }

    fun validateMove(session: GameSession, move: Pos): Option<InvalidKind> =
        session.board.validateMove(move.idx())
            .toOption()
            .orElse {
                Option.cond(session is OpeningSession && !session.validateMove(move)) {
                    Notation.InvalidKind.Forbidden
                }
            }

    fun makeMove(session: RenjuSession, pos: Pos): RenjuSession {
        val thenBoard = session.board.makeMove(pos)

        return thenBoard.winner().fold(
            { session.next(thenBoard, pos, Option.Empty, MessageBufferKey.issue()) },
            { result ->
                val gameResult = when (result) {
                    is Result.FiveInRow ->
                        GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), session.player, session.nextPlayer)
                    else -> GameResult.Full
                }

                session.next(thenBoard, pos, Option.Some(gameResult), session.messageBufferKey)
            }
        )
    }

    suspend fun makeAiMove(session: AiGameSession, resRenjuClient: ResRenjuClient): AiGameSession {
        val (aiMove, solutionNode) = session.solution
            .flatMap { solutionNode ->
                solutionNode.child().get(session.board.lastMove()).fold(
                    { Option.Empty },
                    { Option.Some(it) }
                )
            }
            .fold(
                onDefined = {
                    when (it) {
                        is SolutionNode -> tuple(it.idx(), Option.Some(it))
                        else -> tuple(it.idx(), Option.Empty)
                    }
                },
                onEmpty = {
                    val solution = when (session.aiLevel) {
                        AiLevel.AMOEBA -> FocusSolver.findSolution(session.board)
                        else -> resRenjuClient.update(session.resRenjuToken, session.board)
                    }

                    when (solution) {
                        is SolutionNode -> tuple(solution.idx(), Option.Some(solution))
                        else -> tuple(solution.idx(), Option.Empty)
                    }
                },
            )

        val thenBoard = session.board.makeMove(aiMove)

        return thenBoard.winner().fold(
            {
                session.copy(
                    board = thenBoard,
                    history = session.history + Pos.fromIdx(aiMove),
                    solution = solutionNode
                )
            },
            { result ->
                val gameResult = when (result) {
                    is Result.FiveInRow ->
                        GameResult.Win(GameResult.Cause.FIVE_IN_A_ROW, result.winner(), aiUser, session.owner)
                    else -> GameResult.Full
                }

                session.copy(
                    board = thenBoard,
                    gameResult = Option.Some(gameResult),
                    history = session.history + Pos.fromIdx(aiMove),
                )
            }
        )
    }

    fun resignSession(session: GameSession, cause: GameResult.Cause, user: User): Pair<RenjuSession, GameResult.Win> =
        when (session) {
            is AiGameSession -> {
                val winColor = when (session.board) {
                    is EmptyBoard ->
                        if(session.ownerHasBlack) Notation.Color.White
                        else Notation.Color.Black
                    else -> session.board.color()
                }

                val result = GameResult.Win(cause, winColor, aiUser, session.owner)

                tuple(session.copy(gameResult = Option.Some(result)), result)
            }
            is OpeningSession, is PvpGameSession -> {
                val winColor =
                    if ((session.player.id == user.id) == session.ownerHasBlack) Notation.Color.White
                    else Notation.Color.Black

                val result =
                    if (user.id == session.owner.id)
                        GameResult.Win(cause, winColor, session.opponent, session.owner)
                    else
                        GameResult.Win(cause, winColor, session.owner, session.opponent)

                tuple(session.updateResult(result), result)
            }
        }

    suspend fun finishSession(bot: BotContext, guild: Guild, session: GameSession, result: GameResult) {
        SessionManager.removeGameSession(bot.sessions, guild, session.owner.id)

        session.extractGameRecord(guild.id).forEach { record ->
            GameRecordRepository.uploadGameRecord(bot.dbConnection, record)
        }

        if (session is AiGameSession && session.aiLevel != AiLevel.AMOEBA) {
            bot.resRenjuClient.report(session.resRenjuToken, session.board, result)
        }
    }

}
