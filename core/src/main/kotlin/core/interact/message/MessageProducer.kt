package core.interact.message

import core.assets.*
import core.database.entities.SimpleProfile
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.session.entities.GameSession
import jrenju.Board
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import scala.Enumeration
import utils.structs.IO

abstract class MessageProducer<A, B> {

    abstract val focusWidth: Int

    // BOARD

    protected fun unicodeStone(color: Enumeration.Value) =
        if (color == Color.BLACK()) UNICODE_BLACK_CIRCLE else UNICODE_WHITE_CIRCLE

    private fun User.withColor(color: Enumeration.Value) =
        "${this.name}${this@MessageProducer.unicodeStone(color)}"

    protected fun GameSession.ownerWithColor() =
        if (this.ownerHasBlack) this.owner.withColor(Color.BLACK()) else this.owner.withColor(Color.WHITE())

    protected fun GameSession.opponentWithColor() =
        if (this.ownerHasBlack) this.opponent.withColor(Color.WHITE()) else opponent.withColor(Color.BLACK())

    fun generateFocusedField(board: Board, focus: Pos): FocusedFields {
        val kernelHalf = this.focusWidth / 2
        return (-kernelHalf .. kernelHalf).map { rowOffset ->
            (-kernelHalf .. kernelHalf).map { colOffset ->
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

    abstract fun generateFocusedButtons(focusedFields: FocusedFields): B

    abstract fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageAction<A, B>>

    abstract fun produceSessionArchive(publisher: MessagePublisher<A, B>, session: GameSession): IO<MessageAction<A, B>>

    abstract fun attachFocusButtons(boardAction: MessageAction<A, B>, session: GameSession, focus: Pos): MessageAction<A, B>

    abstract fun attachNavigators(flow: Flow<String>, message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit>

    private val focusNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)

    fun attachFocusNavigators(message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit> =
        this.attachNavigators(this.focusNavigatorFlow, message, checkTerminated)

    private val binaryNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_RIGHT)

    fun attachBinaryNavigators(message: MessageAdaptor<A, B>): IO<Unit> =
        this.attachNavigators(this.binaryNavigatorFlow, message) { false }

    // GAME

    abstract fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): IO<MessageAction<A, B>>

    abstract fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean): IO<MessageAction<A, B>>

    abstract fun produceNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos): IO<MessageAction<A, B>>

    abstract fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos): IO<MessageAction<A, B>>

    abstract fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    abstract fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageAction<A, B>>

    abstract fun produceNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<A, B>>

    abstract fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<A, B>>

    abstract fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<A, B>>

    abstract fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<A, B>>

    abstract fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<A, B>>

    // HELP

    abstract fun produceGuideKit(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    abstract fun produceWelcomeKit(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    abstract fun produceCommandGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    // RANK

    abstract fun produceRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: Set<SimpleProfile>): IO<MessageAction<A, B>>

    // RATING

    abstract fun produceRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    // LANG

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun produceLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageAction<A, B>>

    abstract fun produceLanguageNotFound(publisher: MessagePublisher<A, B>): IO<MessageAction<A, B>>

    abstract fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    // STYLE

    abstract fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    abstract fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    abstract fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): IO<MessageAction<A, B>>

    // POLICY

    // SESSION

    abstract fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<A, B>>

    // START

    abstract fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<A, B>>

    abstract fun produceOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    abstract fun produceRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    abstract fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    abstract fun produceOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    // SET

    abstract fun produceOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, player: User): IO<MessageAction<A, B>>

    abstract fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageAction<A, B>>

    abstract fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos): IO<MessageAction<A, B>>

    abstract fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte): IO<MessageAction<A, B>>

    // REQUEST

    abstract fun produceRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    abstract fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<A, B>>

    // UTILS

    abstract fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer, officialChannel: String): IO<MessageAction<A, B>>

}
