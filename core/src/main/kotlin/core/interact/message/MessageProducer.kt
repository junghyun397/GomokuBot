package core.interact.message

import core.assets.User
import core.database.entities.Announce
import core.database.entities.UserStats
import core.inference.FocusSolver
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.session.GameResult
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import kotlinx.coroutines.flow.Flow
import renju.Board
import renju.notation.Pos
import utils.structs.IO
import utils.structs.Option

interface MessageProducer<A, B> {

    val focusWidth: Int
    val focusRange: IntRange

    // BOARD

    fun generateFocusedField(board: Board, focusInfo: FocusSolver.FocusInfo): FocusedFields

    fun generateFocusedButtons(focusedFields: FocusedFields): B

    fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): MessageBuilder<A, B>

    fun produceSessionArchive(publisher: MessagePublisher<A, B>, session: GameSession, result: Option<GameResult>, animate: Boolean): MessageBuilder<A, B>

    fun attachFocusButtons(boardAction: MessageBuilder<A, B>, session: GameSession, focusInfo: FocusSolver.FocusInfo): MessageBuilder<A, B>

    fun attachFocusButtons(publisher: ComponentPublisher<A, B>, session: GameSession, focusInfo: FocusSolver.FocusInfo): MessageBuilder<A, B>

    fun attachNavigators(flow: Flow<String>, message: A, checkTerminated: suspend () -> Boolean): IO<Unit>

    fun attachFocusNavigators(message: A, checkTerminated: suspend () -> Boolean): IO<Unit>

    fun attachBinaryNavigators(message: A): IO<Unit>

    // GAME

    fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): MessageBuilder<A, B>

    fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean): MessageBuilder<A, B>

    fun produceNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, lastMove: Pos): MessageBuilder<A, B>

    fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User, lastMove: Pos): MessageBuilder<A, B>

    fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User): MessageBuilder<A, B>

    fun produceTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User): MessageBuilder<A, B>

    fun produceNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos): MessageBuilder<A, B>

    fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos): MessageBuilder<A, B>

    fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos): MessageBuilder<A, B>

    fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): MessageBuilder<A, B>

    fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): MessageBuilder<A, B>

    fun produceTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User): MessageBuilder<A, B>

    // HELP

    fun produceHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): MessageBuilder<A, B>

    fun paginateHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): MessageBuilder<A, B>

    fun produceSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): MessageBuilder<A, B>

    fun paginateSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): MessageBuilder<A, B>

    // RANK

    fun produceRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: List<Pair<User, UserStats>>): MessageBuilder<A, B>

    fun produceUserNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // RATING

    fun produceRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // LANG

    fun produceLanguageGuide(publisher: MessagePublisher<A, B>): MessageBuilder<A, B>

    fun produceLanguageNotFound(publisher: MessagePublisher<A, B>): MessageBuilder<A, B>

    fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // STYLE

    fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): MessageBuilder<A, B>

    // CONFIG

    fun produceSettingApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String): MessageBuilder<A, B>

    // SESSION

    fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // START

    fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun produceOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    fun produceRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    fun produceOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    // SET

    fun produceOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User): MessageBuilder<A, B>

    fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos): MessageBuilder<A, B>

    fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos, forbiddenFlag: Byte): MessageBuilder<A, B>

    // REQUEST

    fun produceRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun produceRequestInvalidated(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun produceRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    // UTILS

    fun produceDebugMessage(publisher: MessagePublisher<A, B>, payload: String): MessageBuilder<A, B>

    fun produceAnnounce(publisher: MessagePublisher<A, B>, container: LanguageContainer, announce: Announce): MessageBuilder<A, B>

    fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer, officialChannel: String): MessageBuilder<A, B>

}
