package core.interact.message

import arrow.core.raise.Effect
import core.assets.*
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.mintaka.FocusSolver
import core.session.Rule
import core.session.entities.GameSession
import core.session.entities.OpeningSession
import core.session.entities.SelectStageOpeningSession
import kotlinx.coroutines.flow.flowOf
import renju.notation.Color
import renju.notation.ForbiddenKind
import renju.notation.Pos
import utils.assets.MarkdownAnchorMapping
import utils.assets.SimplifiedMarkdownDocument
import utils.assets.parseSimplifiedMarkdownDocument
import utils.lang.tuple

abstract class MessagingServiceImpl : MessagingService {

    // INTERFACE

    abstract fun sendString(text: String, publisher: MessagePublisher): MessageBuilder

    abstract fun User.asMentionFormat(): String

    abstract fun String.asHighlightFormat(): String

    abstract fun String.asBoldFormat(): String

    // FORMAT

    protected infix fun MessagePublisher.sends(message: String) =
        this@MessagingServiceImpl.sendString(message, this)

    protected fun unicodeStone(color: Color) =
        if (color == Color.Black) UNICODE_BLACK_CIRCLE else UNICODE_WHITE_CIRCLE

    protected fun User.withColor(color: Color) =
        "${this.name}${this@MessagingServiceImpl.unicodeStone(color)}"

    protected fun GameSession.blackPlayerWithColor() =
        this.blackPlayer.withColor(Color.Black)

    protected fun GameSession.whitePlayerWithColor() =
        this.whitePlayer.withColor(Color.White)

    override fun generateFocusedField(session: GameSession, focusInfo: FocusSolver.FocusInfo): FocusedFields {
        val half = this.focusWidth / 2
        val lastMove = session.history.lastAction

        fun focusedButtonFlag(pos: Pos): ButtonFlag =
            when (val stone = session.board.stoneKind(pos)) {
                Color.Black ->
                    if (pos == lastMove) ButtonFlag.BLACK_RECENT
                    else ButtonFlag.BLACK
                Color.White ->
                    if (pos == lastMove) ButtonFlag.WHITE_RECENT
                    else ButtonFlag.WHITE
                null -> when {
                    session.board.playerColor == Color.Black && session.board.forbiddenKind(pos) != null ->
                        ButtonFlag.FORBIDDEN
                    pos in focusInfo.highlights ->
                        ButtonFlag.HIGHLIGHTED
                    session is OpeningSession && !session.validateMove(pos) ->
                        ButtonFlag.DISABLED
                    session is SelectStageOpeningSession ->
                        ButtonFlag.HIGHLIGHTED
                    else ->
                        ButtonFlag.FREE
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

    // RANK

    override fun buildUserNotFound(publisher: MessagePublisher, container: LanguageContainer) =
        publisher sends container.rankErrorNotFound()

    // LANG

    override fun buildLanguageNotFound(publisher: MessagePublisher) =
        publisher sends "There is an error in the Language Code. Please select from the list below."

    override fun buildLanguageUpdated(publisher: MessagePublisher, container: LanguageContainer) =
        publisher sends container.languageUpdated()

    // GAME

    override fun buildBeginsPVP(publisher: MessagePublisher, container: LanguageContainer, blackPlayer: User, whitePlayer: User) =
        publisher sends container.beginPVP(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat())

    override fun buildBeginsPVE(publisher: MessagePublisher, container: LanguageContainer, owner: User, humanHasBlack: Boolean) =
        publisher sends when {
            humanHasBlack -> container.beginPVEAiWhite(owner.asMentionFormat())
            else -> container.beginPVEAiBlack(owner.asMentionFormat())
        }

    override fun buildBeginsOpening(publisher: MessagePublisher, container: LanguageContainer, blackPlayer: User, whitePlayer: User, rule: Rule) =
        publisher sends container.beginOpening(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat())

    override fun buildNextMovePVP(publisher: MessagePublisher, container: LanguageContainer, previousPlayer: User, nextPlayer: User, lastMove: Pos) =
        publisher sends container.processNextPVP(nextPlayer.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildNextMoveOpening(publisher: MessagePublisher, container: LanguageContainer, lastMove: Pos): MessageBuilder =
        publisher sends container.processNextOpening(lastMove.toString().asHighlightFormat())

    override fun buildWinPVP(publisher: MessagePublisher, container: LanguageContainer, winner: User, loser: User, lastMove: Pos) =
        publisher sends container.endPVPWin(winner.asMentionFormat(), loser.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildTiePVP(publisher: MessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat())

    override fun buildSurrenderedPVP(publisher: MessagePublisher, container: LanguageContainer, winner: User, loser: User) =
        publisher sends container.endPVPResign(winner.asMentionFormat(), loser.asMentionFormat())

    override fun buildTimeoutPVP(publisher: MessagePublisher, container: LanguageContainer, winner: User, loser: User) =
        publisher sends container.endPVPTimeOut(winner.asMentionFormat(), loser.asMentionFormat())

    override fun buildNextMovePVE(publisher: MessagePublisher, container: LanguageContainer, owner: User, lastMove: Pos) =
        publisher sends container.processNextPVE(lastMove.toString().asHighlightFormat())

    override fun buildWinPVE(publisher: MessagePublisher, container: LanguageContainer, owner: User, lastMove: Pos) =
        publisher sends container.endPVEWin(owner.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildLosePVE(publisher: MessagePublisher, container: LanguageContainer, owner: User, lastMove: Pos) =
        publisher sends container.endPVELose(owner.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildTiePVE(publisher: MessagePublisher, container: LanguageContainer, owner: User) =
        publisher sends container.endPVETie(owner.asMentionFormat())

    override fun buildSurrenderedPVE(publisher: MessagePublisher, container: LanguageContainer, owner: User) =
        publisher sends container.endPVEResign(owner.asMentionFormat())

    override fun buildTimeoutPVE(publisher: MessagePublisher, container: LanguageContainer, player: User) =
        publisher sends container.endPVETimeOut(player.asMentionFormat())

    // CONFIG

    override fun buildSettingApplied(publisher: MessagePublisher, container: LanguageContainer, configKind: String, configChoice: String) =
        publisher sends container.settingApplied(configKind.asHighlightFormat(), configChoice.asHighlightFormat())

    // SESSION

    override fun buildSessionNotFound(publisher: MessagePublisher, container: LanguageContainer) =
        publisher sends container.sessionNotFound()

    // START

    override fun buildSessionAlready(publisher: MessagePublisher, container: LanguageContainer) =
        publisher sends container.startErrorSessionAlready()

    override fun buildOpponentSessionAlready(publisher: MessagePublisher, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorOpponentSessionAlready(opponent.asMentionFormat())

    override fun buildRequestAlreadySent(publisher: MessagePublisher, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorRequestAlreadySent(opponent.asMentionFormat())

    override fun buildRequestAlready(publisher: MessagePublisher, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorRequestAlready(opponent.asMentionFormat())

    override fun buildOpponentRequestAlready(publisher: MessagePublisher, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorOpponentRequestAlready(opponent.asMentionFormat())

    // SET

    override fun buildSetOrderFailure(publisher: MessagePublisher, container: LanguageContainer, player: User) =
        publisher sends container.processErrorOrder(player.asMentionFormat())

    override fun buildSetIllegalArgumentFailure(publisher: MessagePublisher, container: LanguageContainer) =
        publisher sends container.setErrorIllegalArgument()

    override fun buildSetAlreadyExistFailure(publisher: MessagePublisher, container: LanguageContainer, pos: Pos) =
        publisher sends container.setErrorExist(pos.toString().asHighlightFormat())

    override fun buildSetForbiddenMoveFailure(publisher: MessagePublisher, container: LanguageContainer, pos: Pos, forbiddenKind: ForbiddenKind?) =
        publisher sends container.setErrorForbidden(pos.toString().asHighlightFormat(), forbiddenKindToText(forbiddenKind).asHighlightFormat())

    // STYLE

    override fun buildStyleNotFound(publisher: MessagePublisher, container: LanguageContainer) =
        publisher sends container.styleErrorNotfound()

    override fun buildStyleUpdated(publisher: MessagePublisher, container: LanguageContainer, style: String) =
        publisher sends container.styleUpdated(style)

    // REQUEST

    override fun buildRequestRejected(publisher: MessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.requestRejected(owner.asMentionFormat(), opponent.asMentionFormat())

    override fun buildRequestExpired(publisher: MessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.requestExpired(owner.asMentionFormat(), opponent.asMentionFormat())

    // UTILS

    override fun buildDebugMessage(publisher: MessagePublisher, payload: String) =
        publisher sends payload

    override fun buildNotYetImplemented(publisher: MessagePublisher, container: LanguageContainer) =
        this.buildSomethingWrongMessage(publisher, container, container.notYetImplementedEmbedDescription())

    override fun buildUnableToReplay(publisher: MessagePublisher, container: LanguageContainer) =
        this.buildSomethingWrongMessage(publisher, container, container.replayEmbedUnableToReplayDescription())

    companion object {

        val aboutRenjuDocument: Map<LanguageContainer, Pair<SimplifiedMarkdownDocument, MarkdownAnchorMapping>> =
            Language.entries.associate { language ->
                language.container to parseSimplifiedMarkdownDocument(language.container.aboutRenjuDocument())
            }

    }

}
