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
import utils.lang.memoize
import utils.structs.IO

abstract class MessageProducerImpl<A, B> : MessageProducer<A, B> {

    // INTERFACE

    abstract fun sendString(text: String, publisher: MessagePublisher<A, B>): IO<MessageBuilder<A, B>>

    abstract fun User.asMentionFormat(): String

    abstract fun String.asHighlightFormat(): String

    abstract fun String.asBoldFormat(): String

    // FORMAT

    protected infix fun String.sendBy(publisher: MessagePublisher<A, B>) =
        this@MessageProducerImpl.sendString(this, publisher)

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
                "${(97 + pos.col()).toChar()}${pos.row() + 1}" to flag
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
    // LANG

    protected val languageList =
        buildString {
            Language.values().forEach { language ->
                append(" ``${language.container.languageCode()}``")
            }
        }

    override fun produceLanguageNotFound(publisher: MessagePublisher<A, B>) =
        "There is an error in the Language Code. Please select from the list below." sendBy publisher

    override fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        container.languageUpdated() sendBy publisher

    // GAME

    override fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): IO<MessageBuilder<A, B>> =
        container.beginPVP(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat()) sendBy publisher

    override fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean) =
        when {
            ownerHasBlack -> container.beginPVEAiWhite(owner.asMentionFormat())
            else -> container.beginPVEAiBlack(owner.asMentionFormat())
        } sendBy publisher

    override fun produceNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos) =
        container.processNextPVP(previousPlayer.asMentionFormat(), nextPlayer.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()) sendBy publisher

    override fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos) =
        container.endPVPWin(winner.asMentionFormat(), looser.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()) sendBy publisher

    override fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

    override fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User) =
        container.endPVPResign(winner.asMentionFormat(), looser.asMentionFormat()) sendBy publisher

    override fun produceTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User) =
        container.endPVPTimeOut(winner.asMentionFormat(), looser.asMentionFormat()) sendBy publisher

    override fun produceNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos) =
        container.processNextPVE(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()) sendBy publisher

    override fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos) =
        container.endPVEWin(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()) sendBy publisher

    override fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos) =
        container.endPVELose(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()) sendBy publisher

    override fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        container.endPVETie(owner.asMentionFormat()) sendBy publisher

    override fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        container.endPVEResign(owner.asMentionFormat()) sendBy publisher

    override fun produceTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User) =
        container.endPVETimeOut(player.asMentionFormat()) sendBy publisher

    // CONFIG

    override fun produceConfigApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String) =
        container.settingApplied(configChoice.asHighlightFormat()) sendBy publisher

    // SESSION

    override fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User) =
        container.sessionNotFound(user.asMentionFormat()) sendBy publisher

    // START

    override fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        container.startErrorSessionAlready(owner.asMentionFormat()) sendBy publisher

    override fun produceOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.startErrorOpponentSessionAlready(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

    override fun produceRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.startErrorRequestAlreadySent(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

    override fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.startErrorRequestAlready(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

    override fun produceOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.startErrorOpponentRequestAlready(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

    // SET

    override fun produceOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, player: User) =
        container.processErrorOrder(user.asMentionFormat(), player.asMentionFormat()) sendBy publisher

    override fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User) =
        container.setErrorIllegalArgument(user.asMentionFormat()) sendBy publisher

    override fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos) =
        container.setErrorExist(user.asMentionFormat(), pos.toCartesian().asHighlightFormat()) sendBy publisher

    override fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte) =
        container.setErrorForbidden(user.asMentionFormat(), pos.toCartesian().asHighlightFormat(), forbiddenFlagToText(forbiddenFlag).asHighlightFormat()) sendBy publisher

    // STYLE

    override fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User) =
        container.styleErrorNotfound(user.asMentionFormat()) sendBy publisher

    override fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String) =
        container.styleUpdated(style) sendBy publisher

    // REQUEST

    override fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.requestRejected(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

    override fun produceRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        container.requestExpired(owner.asMentionFormat(), opponent.asMentionFormat()) sendBy publisher

}
