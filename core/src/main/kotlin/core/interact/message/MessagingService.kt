package core.interact.message

import arrow.core.raise.Effect
import core.assets.MessageRef
import core.assets.User
import core.database.entities.Announce
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.database.entities.UserStats
import core.engine.FocusSolver
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.HistoryRenderType
import core.session.entities.ChannelConfig
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.GameSession
import core.session.entities.Rule
import kotlinx.coroutines.flow.Flow
import renju.notation.ColorContainer
import renju.notation.ForbiddenKind
import renju.notation.Pos

interface MessagePayload

typealias MessageComponents = List<*>

typealias MessagePublisher = (MessagePayload) -> MessageBuilder

typealias MessageEditPublisher = (MessageRef) -> MessagePublisher

typealias ComponentPublisher = (MessageComponents) -> MessageBuilder

interface PublisherSet {

    val plain: MessagePublisher

    val windowed: MessagePublisher

    val edit: MessageEditPublisher

    val component: ComponentPublisher

}

data class AdaptivePublisherSet(
    override val plain: MessagePublisher,
    override val windowed: MessagePublisher,
    override val component: ComponentPublisher = { throw IllegalAccessError() },
    private val editSelf: MessagePublisher = { throw IllegalAccessError() },
    private val editGlobal: MessageEditPublisher = { throw IllegalAccessError() },
    private val selfRef: MessageRef? = null,
) : PublisherSet {

    override val edit: MessageEditPublisher get() = { ref ->
        when (ref) {
            this.selfRef -> this.editSelf
            else -> this.editGlobal(ref)
        }
    }

}

data class MonoPublisherSet(
    private val publisher: MessagePublisher,
    private val editGlobal: MessageEditPublisher
) : PublisherSet {

    override val plain: MessagePublisher = this.publisher

    override val windowed: MessagePublisher = this.publisher

    override val edit: MessageEditPublisher = this.editGlobal

    override val component: ComponentPublisher get() { throw IllegalAccessError() }

}

interface MessagingService {

    val focusWidth: Int
    val focusRange: IntRange

    // BOARD

    fun generateFocusedField(session: GameSession, focusInfo: FocusSolver.FocusInfo): FocusedFields

    fun buildFocusedButtons(focusedFields: FocusedFields): MessageComponents

    fun buildBoard(publisher: MessagePublisher, container: LanguageContainer, renderer: BoardRenderer, renderType: HistoryRenderType, draw: BoardDraw, session: GameSession?): MessageBuilder

    fun buildSessionArchive(publisher: MessagePublisher, draw: BoardDraw): MessageBuilder

    fun dispatchFocusButtons(publisher: ComponentPublisher, focusedFields: FocusedFields): MessageBuilder

    fun buildSwapButtons(container: LanguageContainer): MessageComponents

    fun buildBranchingButtons(container: LanguageContainer): MessageComponents

    fun buildDeclareButtons(container: LanguageContainer, session: DeclareStageOpeningSession): MessageComponents

    fun attachNavigators(flow: Flow<String>, message: SentMessage, checkTerminated: suspend () -> Boolean): Effect<Nothing, Unit>

    fun attachFocusNavigators(message: SentMessage, checkTerminated: suspend () -> Boolean): Effect<Nothing, Unit>

    fun attachBinaryNavigators(message: SentMessage): Effect<Nothing, Unit>

    // GAME

    fun buildBeginsPvp(publisher: MessagePublisher, container: LanguageContainer, players: ColorContainer<User>): MessageBuilder

    fun buildBeginsEngine(publisher: MessagePublisher, container: LanguageContainer, humanPlayer: User, humanHasBlack: Boolean): MessageBuilder

    fun buildBeginsOpening(publisher: MessagePublisher, container: LanguageContainer, players: ColorContainer<User>, rule: Rule): MessageBuilder

    fun buildNextMovePvp(publisher: MessagePublisher, container: LanguageContainer, lastPlayer: User, lastMove: Pos): MessageBuilder

    fun buildNextMoveOpening(publisher: MessagePublisher, container: LanguageContainer, lastMove: Pos): MessageBuilder

    fun buildWinPvp(publisher: MessagePublisher, container: LanguageContainer, winner: User, loser: User, lastMove: Pos): MessageBuilder

    fun buildTiePvp(publisher: MessagePublisher, container: LanguageContainer, players: ColorContainer<User>): MessageBuilder

    fun buildResignsPvp(publisher: MessagePublisher, container: LanguageContainer, winner: User, loser: User): MessageBuilder

    fun buildTimeoutPvp(publisher: MessagePublisher, container: LanguageContainer, winner: User, loser: User): MessageBuilder

    fun buildNextMoveEngine(publisher: MessagePublisher, container: LanguageContainer, humanPlayer: User, lastMove: Pos): MessageBuilder

    fun buildEngineLose(publisher: MessagePublisher, container: LanguageContainer, humanPlayer: User, lastMove: Pos): MessageBuilder

    fun buildEngineWin(publisher: MessagePublisher, container: LanguageContainer, humanPlayer: User, lastMove: Pos): MessageBuilder

    fun buildEngineTie(publisher: MessagePublisher, container: LanguageContainer, humanPlayer: User): MessageBuilder

    fun buildResignsEngine(publisher: MessagePublisher, container: LanguageContainer, humanPlayer: User): MessageBuilder

    fun buildTimeoutEngine(publisher: MessagePublisher, container: LanguageContainer, player: User): MessageBuilder

    // REPLAY

    fun buildReplayButtons(gameRecordId: GameRecordId, validationKey: String, totalMoves: Int, currentMoves: Int): MessageComponents

    fun buildBackToListButton(): MessageComponents

    fun buildReplayList(publisher: MessagePublisher, container: LanguageContainer, player: User.Human, records: List<GameRecord>): MessageBuilder

    fun buildReplay(publisher: MessagePublisher, container: LanguageContainer, gameRecord: GameRecord): MessageBuilder

    // HELP

    fun buildHelp(publisher: MessagePublisher, container: LanguageContainer, page: Int): MessageBuilder

    fun buildPaginatedHelp(publisher: MessagePublisher, container: LanguageContainer, page: Int): MessageBuilder

    fun buildSettings(publisher: MessagePublisher, config: ChannelConfig, page: Int): MessageBuilder

    fun buildPaginatedSettings(publisher: MessagePublisher, config: ChannelConfig, page: Int): MessageBuilder

    // RANK

    fun buildRankings(publisher: MessagePublisher, container: LanguageContainer, rankings: List<Pair<User, UserStats>>): MessageBuilder

    fun buildUserNotFound(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    // RATING

    fun buildRating(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    // LANG

    fun buildLanguageGuide(publisher: MessagePublisher): MessageBuilder

    fun buildLanguageNotFound(publisher: MessagePublisher): MessageBuilder

    fun buildLanguageUpdated(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    // STYLE

    fun buildStyleGuide(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    fun buildStyleNotFound(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    fun buildStyleUpdated(publisher: MessagePublisher, container: LanguageContainer, style: String): MessageBuilder

    // CONFIG

    fun buildSettingApplied(publisher: MessagePublisher, container: LanguageContainer, configKind: String, configChoice: String): MessageBuilder

    // SESSION

    fun buildSessionNotFound(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    // START

    fun buildSessionAlready(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    fun buildOpponentSessionAlready(publisher: MessagePublisher, container: LanguageContainer, opponent: User): MessageBuilder

    fun buildRequestAlreadySent(publisher: MessagePublisher, container: LanguageContainer, opponent: User): MessageBuilder

    fun buildRequestAlready(publisher: MessagePublisher, container: LanguageContainer, opponent: User): MessageBuilder

    fun buildOpponentRequestAlready(publisher: MessagePublisher, container: LanguageContainer, opponent: User): MessageBuilder

    // SET

    fun buildSetOrderFailure(publisher: MessagePublisher, container: LanguageContainer, player: User): MessageBuilder

    fun buildSetIllegalArgumentFailure(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    fun buildSetAlreadyExistFailure(publisher: MessagePublisher, container: LanguageContainer, pos: Pos): MessageBuilder

    fun buildSetForbiddenMoveFailure(publisher: MessagePublisher, container: LanguageContainer, pos: Pos, forbiddenKind: ForbiddenKind?): MessageBuilder

    // REQUEST

    fun buildRequest(publisher: MessagePublisher, container: LanguageContainer, requester: User.Human, opponent: User.Human, rule: Rule): MessageBuilder

    fun buildRejectedRequest(publisher: MessagePublisher, container: LanguageContainer, requester: User, opponent: User): MessageBuilder

    fun buildRequestRejected(publisher: MessagePublisher, container: LanguageContainer, requester: User, opponent: User): MessageBuilder

    fun buildRequestExpired(publisher: MessagePublisher, container: LanguageContainer, requester: User, opponent: User): MessageBuilder

    // UTILS

    fun buildDebugMessage(publisher: MessagePublisher, payload: String): MessageBuilder

    fun buildAnnounce(publisher: MessagePublisher, container: LanguageContainer, announce: Announce): MessageBuilder

    fun buildSomethingWrongMessage(publisher: MessagePublisher, container: LanguageContainer, message: String): MessageBuilder

    fun buildNotYetImplemented(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    fun buildUnableToReplay(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

}
