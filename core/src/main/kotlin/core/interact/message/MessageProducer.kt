package core.interact.message

import core.assets.User
import core.database.entities.Announce
import core.database.entities.UserStats
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.session.GameResult
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import jrenju.Board
import jrenju.notation.Pos
import kotlinx.coroutines.flow.Flow
import utils.structs.IO
import utils.structs.Option

interface MessageProducer<A, B> {

    val focusWidth: Int
    val focusRange: IntRange

    // BOARD

    fun generateFocusedField(board: Board, focus: Pos): FocusedFields

    fun generateFocusedButtons(focusedFields: FocusedFields): B

    fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageIO<A, B>>

    fun produceSessionArchive(publisher: MessagePublisher<A, B>, session: GameSession, result: Option<GameResult>): IO<MessageIO<A, B>>

    fun attachFocusButtons(boardAction: MessageIO<A, B>, session: GameSession, focus: Pos): MessageIO<A, B>

    fun attachFocusButtons(publisher: ComponentPublisher<A, B>, session: GameSession, focus: Pos): IO<MessageIO<A, B>>

    fun attachNavigators(flow: Flow<String>, message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit>

    fun attachFocusNavigators(message: MessageAdaptor<A, B>, checkTerminated: suspend () -> Boolean): IO<Unit>

    fun attachBinaryNavigators(message: MessageAdaptor<A, B>): IO<Unit>

    // GAME

    fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): IO<MessageIO<A, B>>

    fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean): IO<MessageIO<A, B>>

    fun produceNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos): IO<MessageIO<A, B>>

    fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos): IO<MessageIO<A, B>>

    fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    fun produceSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageIO<A, B>>

    fun produceTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageIO<A, B>>

    fun produceNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageIO<A, B>>

    fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageIO<A, B>>

    fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageIO<A, B>>

    fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageIO<A, B>>

    fun produceSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageIO<A, B>>

    fun produceTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User): IO<MessageIO<A, B>>

    // HELP

    fun produceHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): IO<MessageIO<A, B>>

    fun paginateHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): IO<MessageIO<A, B>>

    fun produceSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): IO<MessageIO<A, B>>

    fun paginateSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): IO<MessageIO<A, B>>

    // RANK

    fun produceRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: List<Pair<User, UserStats>>): IO<MessageIO<A, B>>

    fun produceUserNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    // RATING

    fun produceRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    // LANG

    fun produceLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageIO<A, B>>

    fun produceLanguageNotFound(publisher: MessagePublisher<A, B>): IO<MessageIO<A, B>>

    fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    // STYLE

    fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageIO<A, B>>

    fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageIO<A, B>>

    fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): IO<MessageIO<A, B>>

    // CONFIG

    fun produceConfigApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String): IO<MessageIO<A, B>>

    // SESSION

    fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageIO<A, B>>

    // START

    fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageIO<A, B>>

    fun produceOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    fun produceRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    fun produceOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    // SET

    fun produceOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, player: User): IO<MessageIO<A, B>>

    fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User): IO<MessageIO<A, B>>

    fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos): IO<MessageIO<A, B>>

    fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte): IO<MessageIO<A, B>>

    // REQUEST

    fun produceRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    fun produceRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    fun produceRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageIO<A, B>>

    // UTILS

    fun produceAnnounce(publisher: MessagePublisher<A, B>, container: LanguageContainer, announce: Announce): IO<MessageIO<A, B>>

    fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer, officialChannel: String): IO<MessageIO<A, B>>

}
