package core.interact.message

import core.assets.User
import core.database.entities.Announce
import core.database.entities.UserStats
import core.inference.FocusSolver
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.HistoryRenderType
import core.session.Rule
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.GameResult
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import kotlinx.coroutines.flow.Flow
import renju.notation.Pos
import utils.structs.IO
import utils.structs.Option

interface MessagingService<A, B> {

    val focusWidth: Int
    val focusRange: IntRange

    // BOARD

    fun generateFocusedField(session: GameSession, focusInfo: FocusSolver.FocusInfo): FocusedFields

    fun generateFocusedButtons(focusedFields: FocusedFields): B

    fun buildBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, renderType: HistoryRenderType, session: GameSession): MessageBuilder<A, B>

    fun buildSessionArchive(publisher: MessagePublisher<A, B>, session: GameSession, result: Option<GameResult>, animate: Boolean): MessageBuilder<A, B>

    fun attachFocusButtons(boardAction: MessageBuilder<A, B>, focusedFields: FocusedFields): MessageBuilder<A, B>

    fun attachFocusButtons(publisher: ComponentPublisher<A, B>, focusedFields: FocusedFields): MessageBuilder<A, B>

    fun attachSwapButtons(boardAction: MessageBuilder<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun attachBranchingButtons(boardAction: MessageBuilder<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun attachDeclareButtons(boardAction: MessageBuilder<A, B>, container: LanguageContainer, session: DeclareStageOpeningSession): MessageBuilder<A, B>

    fun attachNavigators(flow: Flow<String>, message: A, checkTerminated: suspend () -> Boolean): IO<Unit>

    fun attachFocusNavigators(message: A, checkTerminated: suspend () -> Boolean): IO<Unit>

    fun attachBinaryNavigators(message: A): IO<Unit>

    // GAME

    fun buildBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): MessageBuilder<A, B>

    fun buildBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean): MessageBuilder<A, B>

    fun buildBeginsOpening(publisher: MessagePublisher<A, B>, container: LanguageContainer, blackPlayer: User, whitePlayer: User, rule: Rule): MessageBuilder<A, B>

    fun buildNextMovePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, lastMove: Pos): MessageBuilder<A, B>

    fun buildNextMoveOpening(publisher: MessagePublisher<A, B>, container: LanguageContainer, lastMove: Pos): MessageBuilder<A, B>

    fun buildWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User, lastMove: Pos): MessageBuilder<A, B>

    fun buildTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun buildSurrenderedPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User): MessageBuilder<A, B>

    fun buildTimeoutPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, loser: User): MessageBuilder<A, B>

    fun buildNextMovePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos): MessageBuilder<A, B>

    fun buildWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos): MessageBuilder<A, B>

    fun buildLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, lastMove: Pos): MessageBuilder<A, B>

    fun buildTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): MessageBuilder<A, B>

    fun buildSurrenderedPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): MessageBuilder<A, B>

    fun buildTimeoutPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User): MessageBuilder<A, B>

    // HELP

    fun buildHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): MessageBuilder<A, B>

    fun buildPaginatedHelp(publisher: MessagePublisher<A, B>, container: LanguageContainer, page: Int): MessageBuilder<A, B>

    fun buildSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): MessageBuilder<A, B>

    fun buildPaginatedSettings(publisher: MessagePublisher<A, B>, config: GuildConfig, page: Int): MessageBuilder<A, B>

    // RANK

    fun buildRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: List<Pair<User, UserStats>>): MessageBuilder<A, B>

    fun buildUserNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // RATING

    fun buildRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // LANG

    fun buildLanguageGuide(publisher: MessagePublisher<A, B>): MessageBuilder<A, B>

    fun buildLanguageNotFound(publisher: MessagePublisher<A, B>): MessageBuilder<A, B>

    fun buildLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // STYLE

    fun buildStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun buildStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun buildStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): MessageBuilder<A, B>

    // CONFIG

    fun buildSettingApplied(publisher: MessagePublisher<A, B>, container: LanguageContainer, configKind: String, configChoice: String): MessageBuilder<A, B>

    // SESSION

    fun buildSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    // START

    fun buildSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun buildOpponentSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    fun buildRequestAlreadySent(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    fun buildRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    fun buildOpponentRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): MessageBuilder<A, B>

    // SET

    fun buildSetOrderFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, player: User): MessageBuilder<A, B>

    fun buildSetIllegalArgumentFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer): MessageBuilder<A, B>

    fun buildSetAlreadyExistFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos): MessageBuilder<A, B>

    fun buildSetForbiddenMoveFailure(publisher: MessagePublisher<A, B>, container: LanguageContainer, pos: Pos, forbiddenFlag: Byte): MessageBuilder<A, B>

    // REQUEST

    fun buildRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User, rule: Rule): MessageBuilder<A, B>

    fun buildRejectedRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun buildRequestRejected(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    fun buildRequestExpired(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): MessageBuilder<A, B>

    // UTILS

    fun buildDebugMessage(publisher: MessagePublisher<A, B>, payload: String): MessageBuilder<A, B>

    fun buildAnnounce(publisher: MessagePublisher<A, B>, container: LanguageContainer, announce: Announce): MessageBuilder<A, B>

    fun buildNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer, officialChannel: String): MessageBuilder<A, B>

}
