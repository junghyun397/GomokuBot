package core.interact.message

import core.assets.*
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.session.entities.GameSession
import jrenju.Board
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import kotlinx.coroutines.flow.flowOf
import scala.Enumeration
import utils.assets.MarkdownLikeDocument
import utils.assets.parseMarkdownLikeDocument
import utils.lang.and
import utils.lang.memoize
import utils.structs.IO

abstract class MessageProducerImpl<A, B> : MessageProducer<A, B> {

    // INTERFACE

    abstract fun sendString(text: String, publisher: MessagePublisher<A, B>): MessageIO<A, B>

    abstract fun User.asMentionFormat(): String

    abstract fun String.asHighlightFormat(): String

    abstract fun String.asBoldFormat(): String

    // FORMAT

    protected infix fun MessagePublisher<A, B>.sends(message: String) =
        this@MessageProducerImpl.sendString(message, this)

    protected fun unicodeStone(color: Enumeration.Value) =
        if (color == Color.BLACK()) UNICODE_BLACK_CIRCLE else UNICODE_WHITE_CIRCLE

    protected fun User.withColor(color: Enumeration.Value) =
        "${this.name}${this@MessageProducerImpl.unicodeStone(color)}"

    protected fun GameSession.ownerWithColor() =
        if (this.ownerHasBlack) this.owner.withColor(Color.BLACK()) else this.owner.withColor(Color.WHITE())

    protected fun GameSession.opponentWithColor() =
        if (this.ownerHasBlack) this.opponent.withColor(Color.WHITE()) else opponent.withColor(Color.BLACK())

    override fun generateFocusedField(board: Board, focus: Pos): FocusedFields {
        val half = this.focusWidth / 2
        return (-half .. half).map { rowOffset ->
            (-half .. half).map { colOffset ->
                val pos = Pos(focus.row() + rowOffset, focus.col() + colOffset)
                val flag = if (pos.idx() == board.latestMove()) when (board.boardField()[board.latestMove()]) {
                    Flag.BLACK() -> ButtonFlag.BLACK_RECENT
                    Flag.WHITE() -> ButtonFlag.WHITE_RECENT
                    else -> ButtonFlag.FREE
                } else when (board.boardField()[pos.idx()]) {
                    Flag.BLACK() -> ButtonFlag.BLACK
                    Flag.WHITE() -> ButtonFlag.WHITE
                    Flag.FORBIDDEN_33(), Flag.FORBIDDEN_44(), Flag.FORBIDDEN_6() ->
                        if (board.nextColor() == Color.BLACK()) ButtonFlag.FORBIDDEN
                        else ButtonFlag.FREE
                    else ->
                        if (pos.idx() == Renju.BOARD_CENTER_POS().idx()) ButtonFlag.HIGHLIGHTED
                        else ButtonFlag.FREE
                }
                "${(97 + pos.col()).toChar()}${pos.row() + 1}" and flag
            }
        }
    }

    // NAVIGATE

    private val focusNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)

    override fun attachFocusNavigators(message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit> =
        this.attachNavigators(this.focusNavigatorFlow, message, checkTerminated)

    private val binaryNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_RIGHT)

    override fun attachBinaryNavigators(message: MessageAdaptor<A, B>): IO<Unit> =
        this.attachNavigators(this.binaryNavigatorFlow, message) { false }

    // HELP

    protected val aboutRenjuDocument: (LanguageContainer) -> MarkdownLikeDocument = memoize { container ->
        parseMarkdownLikeDocument(container.aboutRenjuDocument())
    }

    // RANK

    override fun produceUserNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.rankErrorNotFound()

    // LANG

    protected val languageList =
        buildString {
            Language.values().forEach { language ->
                append(" ``${language.container.languageCode()}``")
            }
        }

    override fun produceLanguageNotFound(publisher: MessagePublisher<A, B>) =
        publisher sends "There is an error in the Language Code. Please select from the list below."

    override fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.languageUpdated()

    // GAME

    override fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User) =
        publisher sends container.beginPVP(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat())

    override fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean) =
        publisher sends when {
            ownerHasBlack -> container.beginPVEAiWhite(owner.asMentionFormat())
            else -> container.beginPVEAiBlack(owner.asMentionFormat())
        }

    override fun produceNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos) =
        publisher sends container.processNextPVP(
            nextPlayer.asMentionFormat(),
            latestMove.toCartesian().asHighlightFormat()
        )

    override fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos) =
        publisher sends container.endPVPWin(winner.asMentionFormat(), looser.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())

    override fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat())

    override fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User) =
        publisher sends container.endPVPResign(winner.asMentionFormat(), looser.asMentionFormat())

    override fun produceTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User) =
        publisher sends container.endPVPTimeOut(winner.asMentionFormat(), looser.asMentionFormat())

    override fun produceNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos) =
        publisher sends container.processNextPVE(latestMove.toCartesian().asHighlightFormat())

    override fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos) =
        publisher sends container.endPVEWin(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())

    override fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos) =
        publisher sends container.endPVELose(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())

    override fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        publisher sends container.endPVETie(owner.asMentionFormat())

    override fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        publisher sends container.endPVEResign(owner.asMentionFormat())

    override fun produceTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User) =
        publisher sends container.endPVETimeOut(player.asMentionFormat())

    // CONFIG

    override fun produceSettingApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String) =
        publisher sends container.settingApplied(configKind.asHighlightFormat(), configChoice.asHighlightFormat())

    // SESSION

    override fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.sessionNotFound()

    // START

    override fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.startErrorSessionAlready()

    override fun produceOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorOpponentSessionAlready(opponent.asMentionFormat())

    override fun produceRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorRequestAlreadySent(opponent.asMentionFormat())

    override fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorRequestAlready(opponent.asMentionFormat())

    override fun produceOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorOpponentRequestAlready(opponent.asMentionFormat())

    // SET

    override fun produceOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User) =
        publisher sends container.processErrorOrder(player.asMentionFormat())

    override fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.setErrorIllegalArgument()

    override fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos) =
        publisher sends container.setErrorExist(pos.toCartesian().asHighlightFormat())

    override fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos, forbiddenFlag: Byte) =
        publisher sends container.setErrorForbidden(pos.toCartesian().asHighlightFormat(), forbiddenFlagToText(forbiddenFlag).asHighlightFormat())

    override fun produceSetEditMode(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.setErrorEditMode()

    // STYLE

    override fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.styleErrorNotfound()

    override fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String) =
        publisher sends container.styleUpdated(style)

    // REQUEST

    override fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.requestRejected(owner.asMentionFormat(), opponent.asMentionFormat())

    override fun produceRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.requestExpired(owner.asMentionFormat(), opponent.asMentionFormat())

    // UTILS

    override fun produceDebugMessage(publisher: MessagePublisher<A, B>, payload: String) =
        publisher sends payload

}
