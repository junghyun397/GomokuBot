package core.interact.message

import core.assets.UNICODE_BLACK_CIRCLE
import core.assets.UNICODE_WHITE_CIRCLE
import core.assets.User
import core.database.entities.SimpleProfile
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.session.FocusPolicy
import core.session.entities.GameSession
import jrenju.Board
import jrenju.notation.Color
import jrenju.notation.Flag
import jrenju.notation.Pos
import jrenju.notation.Renju
import scala.Enumeration
import utils.structs.IO

abstract class MessageProducer<A, B> {

    // BOARD

    abstract fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageAction<B>>

    abstract fun producePublicBoard(publisher: MessagePublisher<A, B>, session: GameSession): IO<MessageAction<B>>

    protected fun User.withColor(color: Enumeration.Value) =
        "${this.name}${if (color == Color.BLACK()) UNICODE_BLACK_CIRCLE else UNICODE_WHITE_CIRCLE}"

    protected fun GameSession.ownerWithColor() =
        if (this.ownerHasBlack) this.owner.withColor(Color.BLACK()) else this.owner.withColor(Color.WHITE())

    protected fun GameSession.opponentWithColor() =
        if (this.ownerHasBlack) this.opponent.withColor(Color.WHITE()) else opponent.withColor(Color.BLACK())

    @Suppress("SameParameterValue")
    protected fun generateFocusedField(board: Board, focus: Pos, kernelWidth: Int): FocusedFields {
        val kernelHalf = kernelWidth / 2
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
                        if (pos.idx() == Renju.BOARD_CENTER().idx()) ButtonFlag.HIGHLIGHTED
                        else ButtonFlag.FREE
                }
                "${(97 + pos.col()).toChar()}${pos.row() + 1}" to flag
            }
        }
    }

    abstract fun attachFocusButtons(boardAction: MessageAction<B>, container: LanguageContainer, focusPolicy: FocusPolicy, session: GameSession): MessageAction<B>

    // GAME

    abstract fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    abstract fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    abstract fun produceNextMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    abstract fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageAction<B>>

    abstract fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    abstract fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    // HELP

    abstract fun produceAboutBot(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceCommandGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // RANK

    abstract fun produceRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: Set<SimpleProfile>): IO<MessageAction<B>>

    // RATING

    abstract fun produceRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // LANG

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun produceLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageAction<B>>

    abstract fun produceLanguageNotFound(publisher: MessagePublisher<A, B>): IO<MessageAction<B>>

    abstract fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // STYLE

    abstract fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): IO<MessageAction<B>>

    // POLICY

    // SESSION

    abstract fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // START

    abstract fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): IO<MessageAction<B>>

    abstract fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    // SET

    abstract fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageAction<B>>

    abstract fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos): IO<MessageAction<B>>

    abstract fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte): IO<MessageAction<B>>

    // REQUEST

    abstract fun produceRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    abstract fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    // UTILS

    abstract fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

}
