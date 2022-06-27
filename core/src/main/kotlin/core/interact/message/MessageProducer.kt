package core.interact.message

import core.assets.*
import core.database.entities.UserStats
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.session.entities.GameSession
import core.session.entities.GuildConfig
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
    abstract val focusRange: IntRange

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

    abstract fun generateFocusedButtons(focusedFields: FocusedFields): B

    abstract fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageIO<A, B>>

    abstract fun produceSessionArchive(publisher: MessagePublisher<A, B>, session: GameSession): IO<MessageIO<A, B>>

    abstract fun attachFocusButtons(boardAction: MessageIO<A, B>, session: GameSession, focus: Pos): MessageIO<A, B>

    abstract fun attachNavigators(flow: Flow<String>, message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit>

    private val focusNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)

    fun attachFocusNavigators(message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit> =
        this.attachNavigators(this.focusNavigatorFlow, message, checkTerminated)

    private val binaryNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_RIGHT)

    fun attachBinaryNavigators(message: MessageAdaptor<A, B>): IO<Unit> =
        this.attachNavigators(this.binaryNavigatorFlow, message) { false }

    // GAME

    abstract fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): IO<MessageIO<A, B>>

    abstract fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean): IO<MessageIO<A, B>>

    abstract fun produceNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos): IO<MessageIO<A, B>>

    abstract fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos): IO<MessageIO<A, B>>

    abstract fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    abstract fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageIO<A, B>>

    abstract fun produceTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageIO<A, B>>

    abstract fun produceNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageIO<A, B>>

    abstract fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageIO<A, B>>

    abstract fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageIO<A, B>>

    abstract fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageIO<A, B>>

    abstract fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageIO<A, B>>

    abstract fun produceTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User): IO<MessageIO<A, B>>

    // HELP

    abstract fun produceHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): IO<MessageIO<A, B>>

    abstract fun paginateHelp(original: MessageAdaptor<A, B>, container: LanguageContainer, page: Int): IO<MessageIO<A, B>>

    abstract fun produceSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): IO<MessageIO<A, B>>

    abstract fun paginateSettings(original: MessageAdaptor<A, B>, config: GuildConfig, page: Int): IO<MessageIO<A, B>>

    // RANK

    abstract fun produceRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: List<UserStats>): IO<MessageIO<A, B>>

    // RATING

    abstract fun produceRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    // LANG

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun produceLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageIO<A, B>>

    abstract fun produceLanguageNotFound(publisher: MessagePublisher<A, B>): IO<MessageIO<A, B>>

    abstract fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    // STYLE

    abstract fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    abstract fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageIO<A, B>>

    abstract fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): IO<MessageIO<A, B>>

    // CONFIG

    abstract fun produceConfigApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String): IO<MessageIO<A, B>>

    // SESSION

    abstract fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageIO<A, B>>

    // START

    abstract fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageIO<A, B>>

    abstract fun produceOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    abstract fun produceRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    abstract fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    abstract fun produceOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    // SET

    abstract fun produceOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, player: User): IO<MessageIO<A, B>>

    abstract fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageIO<A, B>>

    abstract fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos): IO<MessageIO<A, B>>

    abstract fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte): IO<MessageIO<A, B>>

    // REQUEST

    abstract fun produceRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    abstract fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    abstract fun produceRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    // UTILS

    abstract fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer, officialChannel: String): IO<MessageIO<A, B>>

}
