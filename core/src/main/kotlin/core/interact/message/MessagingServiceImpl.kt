package core.interact.message

import core.assets.*
import core.inference.FocusSolver
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.session.Rule
import core.session.entities.GameSession
import core.session.entities.OpeningSession
import core.session.entities.SelectStageOpeningSession
import kotlinx.coroutines.flow.flowOf
import renju.notation.Color
import renju.notation.Flag
import renju.notation.Pos
import utils.assets.MarkdownAnchorMapping
import utils.assets.SimplifiedMarkdownDocument
import utils.assets.parseSimplifiedMarkdownDocument
import utils.lang.tuple
import utils.structs.IO

abstract class MessagingServiceImpl<A, B> : MessagingService<A, B> {

    // INTERFACE

    abstract fun sendString(text: String, publisher: MessagePublisher<A, B>): MessageBuilder<A, B>

    abstract fun User.asMentionFormat(): String

    abstract fun String.asHighlightFormat(): String

    abstract fun String.asBoldFormat(): String

    // FORMAT

    protected infix fun MessagePublisher<A, B>.sends(message: String) =
        this@MessagingServiceImpl.sendString(message, this)

    protected fun unicodeStone(color: Color) =
        if (color == Notation.Color.Black) UNICODE_BLACK_CIRCLE else UNICODE_WHITE_CIRCLE

    protected fun User.withColor(color: Color) =
        "${this.name}${this@MessagingServiceImpl.unicodeStone(color)}"

    protected fun GameSession.ownerWithColor() =
        if (this.ownerHasBlack) this.owner.withColor(Notation.Color.Black) else this.owner.withColor(Notation.Color.White)

    protected fun GameSession.opponentWithColor() =
        if (this.ownerHasBlack) this.opponent.withColor(Notation.Color.White) else opponent.withColor(Notation.Color.Black)

    override fun generateFocusedField(session: GameSession, focusInfo: FocusSolver.FocusInfo): FocusedFields {
        val half = this.focusWidth / 2

        return (-half .. half).map { rowOffset ->
            (-half .. half).map { colOffset ->
                val absolutePos = Pos(focusInfo.focus.row() + rowOffset, focusInfo.focus.col() + colOffset)

                val buttonFlag = when (val idx = absolutePos.idx()) {
                    session.board.lastMove() -> when (session.board.field()[session.board.lastMove()]) {
                        Flag.BLACK() -> ButtonFlag.BLACK_RECENT
                        Flag.WHITE() -> ButtonFlag.WHITE_RECENT
                        else -> throw IllegalStateException()
                    }
                    else -> when (val flag = session.board.field()[idx]) {
                        Flag.BLACK() -> ButtonFlag.BLACK
                        Flag.WHITE() -> ButtonFlag.WHITE
                        else -> when {
                            Notation.FlagInstance.isForbid(flag, session.board.nextColorFlag()) -> ButtonFlag.FORBIDDEN
                            else -> when (absolutePos) {
                                in focusInfo.highlights -> ButtonFlag.HIGHLIGHTED
                                else -> when (session) {
                                    is OpeningSession ->
                                        if (session.validateMove(absolutePos))
                                            if (session is SelectStageOpeningSession) ButtonFlag.HIGHLIGHTED
                                            else ButtonFlag.FREE
                                        else ButtonFlag.DISABLED
                                    else -> ButtonFlag.FREE
                                }
                            }
                        }
                    }
                }

                tuple(absolutePos.toString(), buttonFlag)
            }
        }
    }

    // NAVIGATE

    private val focusNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS)

    override fun attachFocusNavigators(message: A, checkTerminated: suspend () -> Boolean): IO<Unit> =
        this.attachNavigators(this.focusNavigatorFlow, message, checkTerminated)

    private val binaryNavigatorFlow = flowOf(UNICODE_LEFT, UNICODE_RIGHT)

    override fun attachBinaryNavigators(message: A): IO<Unit> =
        this.attachNavigators(this.binaryNavigatorFlow, message) { false }

    // RANK

    override fun buildUserNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.rankErrorNotFound()

    // LANG

    override fun buildLanguageNotFound(publisher: MessagePublisher<A, B>) =
        publisher sends "There is an error in the Language Code. Please select from the list below."

    override fun buildLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.languageUpdated()

    // GAME

    override fun buildBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User) =
        publisher sends container.beginPVP(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat())

    override fun buildBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean) =
        publisher sends when {
            ownerHasBlack -> container.beginPVEAiWhite(owner.asMentionFormat())
            else -> container.beginPVEAiBlack(owner.asMentionFormat())
        }

    override fun buildBeginsOpening(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User, rule: Rule) =
        publisher sends container.beginOpening(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat())

    override fun buildNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, lastMove: Pos) =
        publisher sends container.processNextPVP(nextPlayer.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildNextMoveOpening(publisher: MessagePublisher<A, B>, container: LanguageContainer, lastMove: Pos): MessageBuilder<A, B> =
        publisher sends container.processNextOpening(lastMove.toString().asHighlightFormat())

    override fun buildWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User, lastMove: Pos) =
        publisher sends container.endPVPWin(winner.asMentionFormat(), loser.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat())

    override fun buildSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User) =
        publisher sends container.endPVPResign(winner.asMentionFormat(), loser.asMentionFormat())

    override fun buildTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User) =
        publisher sends container.endPVPTimeOut(winner.asMentionFormat(), loser.asMentionFormat())

    override fun buildNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos) =
        publisher sends container.processNextPVE(lastMove.toString().asHighlightFormat())

    override fun buildWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos) =
        publisher sends container.endPVEWin(owner.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos) =
        publisher sends container.endPVELose(owner.asMentionFormat(), lastMove.toString().asHighlightFormat())

    override fun buildTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        publisher sends container.endPVETie(owner.asMentionFormat())

    override fun buildSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User) =
        publisher sends container.endPVEResign(owner.asMentionFormat())

    override fun buildTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User) =
        publisher sends container.endPVETimeOut(player.asMentionFormat())

    // CONFIG

    override fun buildSettingApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String) =
        publisher sends container.settingApplied(configKind.asHighlightFormat(), configChoice.asHighlightFormat())

    // SESSION

    override fun buildSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.sessionNotFound()

    // START

    override fun buildSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.startErrorSessionAlready()

    override fun buildOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorOpponentSessionAlready(opponent.asMentionFormat())

    override fun buildRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorRequestAlreadySent(opponent.asMentionFormat())

    override fun buildRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorRequestAlready(opponent.asMentionFormat())

    override fun buildOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User) =
        publisher sends container.startErrorOpponentRequestAlready(opponent.asMentionFormat())

    // SET

    override fun buildSetOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User) =
        publisher sends container.processErrorOrder(player.asMentionFormat())

    override fun buildSetIllegalArgumentFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.setErrorIllegalArgument()

    override fun buildSetAlreadyExistFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos) =
        publisher sends container.setErrorExist(pos.toString().asHighlightFormat())

    override fun buildSetForbiddenMoveFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos, forbiddenFlag: Byte) =
        publisher sends container.setErrorForbidden(pos.toString().asHighlightFormat(), forbiddenFlagToText(forbiddenFlag).asHighlightFormat())

    // STYLE

    override fun buildStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer) =
        publisher sends container.styleErrorNotfound()

    override fun buildStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String) =
        publisher sends container.styleUpdated(style)

    // REQUEST

    override fun buildRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.requestRejected(owner.asMentionFormat(), opponent.asMentionFormat())

    override fun buildRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User) =
        publisher sends container.requestExpired(owner.asMentionFormat(), opponent.asMentionFormat())

    // UTILS

    override fun buildDebugMessage(publisher: MessagePublisher<A, B>, payload: String) =
        publisher sends payload

    companion object {

        val aboutRenjuDocument: Map<LanguageContainer, Pair<SimplifiedMarkdownDocument, MarkdownAnchorMapping>> =
            Language.entries.associate { language ->
                language.container to parseSimplifiedMarkdownDocument(language.container.aboutRenjuDocument())
            }

    }

}
