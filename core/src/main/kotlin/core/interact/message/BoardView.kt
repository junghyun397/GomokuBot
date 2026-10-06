package core.interact.message

import core.assets.User
import core.engine.FocusSolver
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.HistoryRenderType
import core.session.entities.*
import renju.GameState
import renju.notation.Pos

data class BoardView(
    val draw: BoardDraw,
    val container: LanguageContainer,
    val style: BoardStyle,
    val renderType: HistoryRenderType,
    val interaction: BoardInteraction,
    val openingPlayer: User? = null,
    val offers: Set<Pos>? = null,
    val blinds: Set<Pos>? = null,
)

sealed interface BoardInteraction {

    data class Place(val input: BoardInput) : BoardInteraction

    data class Offer(val input: BoardInput, val remaining: Int) : BoardInteraction

    data class Select(val input: BoardInput) : BoardInteraction

    data class Swap(val offerCount: Int?) : BoardInteraction

    data object Branch : BoardInteraction

    data class Declare(val maxCount: Int) : BoardInteraction

    data object Finished : BoardInteraction

}

data class BoardInput(
    val state: GameState,
    val focus: FocusSolver.FocusInfo,
    val legalMoves: Set<Pos>,
    val choices: Set<Pos>,
)

fun GameSession.buildBoardInput(focusInfo: FocusSolver.FocusInfo) = BoardInput(
    state = this.state,
    focus = focusInfo,
    legalMoves = (0 until Pos.BOARD_SIZE).map(Pos::fromIdx).filter { this.isLegalMove(it) }.toSet(),
    choices = if (this is SelectStageOpeningSession) this.moveCandidates.toSet() else emptySet(),
)

fun GameSession.buildBoardView(config: ChannelConfig, focusInfo: FocusSolver.FocusInfo?): BoardView {
    val interaction = when {
        this.gameResult != null -> BoardInteraction.Finished
        this is SwapStageOpeningSession -> BoardInteraction.Swap(this.offerCount)
        this is BranchingStageOpeningSession -> BoardInteraction.Branch
        this is DeclareStageOpeningSession -> BoardInteraction.Declare(this.maxOfferCount)
        else -> {
            val input = this.buildBoardInput(requireNotNull(focusInfo))
            when (this) {
                is OfferStageOpeningSession -> BoardInteraction.Offer(input, this.remainingMoves)
                is SelectStageOpeningSession -> BoardInteraction.Select(input)
                else -> BoardInteraction.Place(input)
            }
        }
    }

    val blinds = when (this) {
        is MoveStageOpeningSession -> (0 until Pos.BOARD_SIZE).map(Pos::fromIdx)
            .filterNot { this.isLegalMove(it) }.toSet()
        is OfferStageOpeningSession -> (0 until Pos.BOARD_SIZE).map(Pos::fromIdx)
            .filter {
                this.state.board.stoneKind(it) == null && this.state.board.forbiddenKind(it) == null && it in this.symmetryMoves
            }.toSet()
        else -> null
    }

    return BoardView(
        draw = this.buildBoardDraw(),
        container = config.language.container,
        style = config.boardStyle,
        renderType = config.markType,
        interaction = interaction,
        openingPlayer = if (this is OpeningSession) this.player else null,
        offers = if (this is NegotiateStageOpeningSession) this.moveCandidates.toSet() else null,
        blinds = blinds,
    )
}
