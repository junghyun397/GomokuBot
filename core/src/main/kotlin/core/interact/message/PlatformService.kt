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
import core.session.entities.*
import kotlinx.coroutines.flow.Flow

interface MessagePayload

data class PlatformMessage(val content: String) : MessagePayload

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

interface PlatformService {

    val focusWidth: Int
    val focusRange: IntRange

    // PLATFORM

    suspend fun upsertCommands(container: LanguageContainer)

    suspend fun bulkDelete(messageRefs: List<MessageRef>)

    suspend fun removeNavigators(messageRef: MessageRef, reduceComponents: Boolean = false)

    suspend fun archiveSession(session: GameSession, policy: ArchivePolicy)

    // FORMAT

    fun formatUser(user: User): String

    fun formatHighlight(text: String): String

    fun formatBold(text: String): String

    // MESSAGE

    fun buildMessage(publisher: MessagePublisher, message: PlatformMessage): MessageBuilder =
        publisher(message)

    // BOARD

    fun generateFocusedField(session: GameSession, focusInfo: FocusSolver.FocusInfo): InputField

    fun buildFocusedButtons(inputField: InputField): MessageComponents

    fun buildBoard(publisher: MessagePublisher, container: LanguageContainer, renderer: BoardRenderer, renderType: HistoryRenderType, draw: BoardDraw, session: GameSession?): MessageBuilder

    fun buildSessionArchive(publisher: MessagePublisher, draw: BoardDraw): MessageBuilder

    fun upsertInputBoard(publisher: ComponentPublisher, inputField: InputField): MessageBuilder

    fun buildSwapButtons(container: LanguageContainer): MessageComponents

    fun buildBranchingButtons(container: LanguageContainer): MessageComponents

    fun buildDeclareButtons(container: LanguageContainer, session: DeclareStageOpeningSession): MessageComponents

    fun attachNavigators(flow: Flow<String>, message: SentMessage, checkTerminated: suspend () -> Boolean): Effect<Nothing, Unit>

    fun attachFocusNavigators(message: SentMessage, checkTerminated: suspend () -> Boolean): Effect<Nothing, Unit>

    fun attachBinaryNavigators(message: SentMessage): Effect<Nothing, Unit>

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

    // RATING

    fun buildRating(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    // LANG

    fun buildLanguageGuide(publisher: MessagePublisher): MessageBuilder

    // STYLE

    fun buildStyleGuide(publisher: MessagePublisher, container: LanguageContainer): MessageBuilder

    // REQUEST

    fun buildRequest(publisher: MessagePublisher, container: LanguageContainer, requester: User.Human, opponent: User.Human, rule: Rule): MessageBuilder

    fun buildRejectedRequest(publisher: MessagePublisher, container: LanguageContainer, requester: User, opponent: User): MessageBuilder

    // UTILS

    fun buildAnnounce(publisher: MessagePublisher, container: LanguageContainer, announce: Announce): MessageBuilder

    fun buildSomethingWrongMessage(publisher: MessagePublisher, container: LanguageContainer, message: String): MessageBuilder

}
