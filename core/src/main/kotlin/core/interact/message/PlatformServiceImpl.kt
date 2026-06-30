package core.interact.message

import arrow.core.raise.Effect
import core.assets.*
import core.engine.FocusSolver
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.session.entities.GameSession
import core.session.entities.OpeningSession
import core.session.entities.SelectStageOpeningSession
import kotlinx.coroutines.flow.flowOf
import renju.notation.Color
import renju.notation.Pos
import utils.MarkdownAnchorMapping
import utils.SimplifiedMarkdownDocument
import utils.parseSimplifiedMarkdownDocument
import utils.tuple

abstract class PlatformServiceImpl : PlatformService {

    // INTERFACE

    protected fun User.asMentionFormat(): String =
        this@PlatformServiceImpl.formatUser(this)

    protected fun String.asHighlightFormat(): String =
        this@PlatformServiceImpl.formatHighlight(this)

    protected fun String.asBoldFormat(): String =
        this@PlatformServiceImpl.formatBold(this)

    // FORMAT

    protected fun User.withColor(color: Color) =
        "${this.name}${UNICODE_STONE[color]}"

    protected fun BoardDraw.playerWithColor() =
        this.recipients.player.first.withColor(this.recipients.player.second)

    protected fun BoardDraw.opponentWithColor() =
        this.recipients.opponent.first.withColor(this.recipients.opponent.second)

    override fun generateFocusedField(session: GameSession, focusInfo: FocusSolver.FocusInfo): InputField {
        val half = this.focusWidth / 2
        val lastMove = session.state.history.lastOrNull()

        fun focusedButtonFlag(pos: Pos): ButtonFlag =
            when (session.state.board.stoneKind(pos)) {
                Color.BLACK ->
                    if (pos == lastMove) ButtonFlag.BLACK_RECENT
                    else ButtonFlag.BLACK
                Color.WHITE ->
                    if (pos == lastMove) ButtonFlag.WHITE_RECENT
                    else ButtonFlag.WHITE
                null -> when {
                    session.state.board.playerColor == Color.BLACK && session.state.board.forbiddenKind(pos) != null ->
                        ButtonFlag.FORBIDDEN
                    focusInfo.highlights?.contains(pos) ?: false ->
                        ButtonFlag.HIGHLIGHTED
                    session is OpeningSession && !session.isLegalMove(pos) ->
                        ButtonFlag.DISABLED
                    session is SelectStageOpeningSession ->
                        ButtonFlag.HIGHLIGHTED
                    else ->
                        ButtonFlag.EMPTY
                }
            }

        return (-half .. half).map { rowOffset ->
            (-half .. half).map { colOffset ->
                val absolutePos = Pos(focusInfo.focus.row + rowOffset, focusInfo.focus.col + colOffset)

                tuple(absolutePos.toString(), focusedButtonFlag(absolutePos))
            }
        }
    }

    // NAVIGATE

    private val focusNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)

    override fun attachFocusNavigators(message: SentMessage, checkTerminated: suspend () -> Boolean): Effect<Nothing, Unit> =
        this.attachNavigators(this.focusNavigatorFlow, message, checkTerminated)

    private val binaryNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_RIGHT)

    override fun attachBinaryNavigators(message: SentMessage): Effect<Nothing, Unit> =
        this.attachNavigators(this.binaryNavigatorFlow, message) { false }

    companion object {

        val aboutRenjuDocument: Map<LanguageContainer, Pair<SimplifiedMarkdownDocument, MarkdownAnchorMapping>> =
            Language.entries.associate { language ->
                language.container to parseSimplifiedMarkdownDocument(language.container.aboutRenjuDocument())
            }

    }

}
