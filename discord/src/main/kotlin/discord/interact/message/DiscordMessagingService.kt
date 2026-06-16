package discord.interact.message

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.*
import core.database.entities.Announce
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.database.entities.UserStats
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.*
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.HistoryRenderType
import core.interact.message.graphics.ImageBoardRenderer
import core.session.entities.*
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import discord.assets.*
import discord.interact.ChannelManager
import discord.interact.message.DiscordMessagingService.IdConvention.ACCEPT
import discord.interact.message.DiscordMessagingService.IdConvention.NO_FEATURE
import discord.interact.message.DiscordMessagingService.IdConvention.OPENING
import discord.interact.message.DiscordMessagingService.IdConvention.REJECT
import discord.interact.message.DiscordMessagingService.IdConvention.REPLAY
import discord.interact.message.DiscordMessagingService.IdConvention.REPLAY_LIST
import discord.interact.message.DiscordMessagingService.IdConvention.SET
import discord.interact.parse.buildableCommands
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.selections.SelectMenu
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import renju.Board
import renju.GameState
import renju.notation.Color
import renju.notation.GameResult
import renju.notation.Pos
import utils.memoize
import utils.tuple
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

object DiscordMessagingService : MessagingServiceImpl() {

    object IdConvention {

        const val NO_FEATURE = 'n'

        const val SET = 's'
        const val ACCEPT = 'a'
        const val REJECT = 'r'
        const val APPLY_SETTING = 'p'
        const val OPENING = 'o'
        const val REPLAY_LIST = 'l'
        const val REPLAY = 'e'

    }

    override val focusWidth = 5
    override val focusRange = 2..(Pos.BOARD_BOUND - 2)

    // INTERFACE

    override fun sendString(text: String, publisher: DiscordMessagePublisher) =
        publisher(DiscordMessageData(text))

    private infix fun DiscordMessagePublisher.sends(embed: MessageEmbed) =
        this(DiscordMessageData(embed = embed))

    private infix fun DiscordMessagePublisher.sends(embeds: List<MessageEmbed>) =
        this(DiscordMessageData(embeds = embeds))

    override fun User.asMentionFormat() =
        if ((this is User.Human && this.isAnonymous) || this is User.GomokuBot)
            this.name
        else
            "<@${(this as User.Human).givenId.idLong}>"

    override fun String.asHighlightFormat() = "``$this``"

    override fun String.asBoldFormat() = "**$this**"

    // FORMAT

    private fun ActionRowChildComponent.liftToButtons() =
        listOf(ActionRow.of(this))

    // BOARD

    private fun InlineEmbed.buildBoardAuthor(container: LanguageContainer, draw: BoardDraw) =
        author {
            iconUrl = draw.users.black.profileURL ?: draw.users.white.profileURL
            name = buildString {
                append(draw.blackPlayerWithColor())
                append(" vs ")
                append(draw.whitePlayerWithColor())
                append(", ")

                when {
                    draw.result != null -> append(container.boardFinished())
//                    session is OpeningSession -> append(container.boardInOpening())
                    else -> append(container.boardInProgress())
                }
            }
        }

    private fun InlineEmbed.buildStatusFields(container: LanguageContainer, draw: BoardDraw) {
        draw.state.history.lastOrNull()
            ?.let { lastPos ->
                field {
                    name = container.boardMoves()
                    value = draw.state.history.size.toString().asHighlightFormat()
                    inline = true
                }

                field {
                    name = container.boardLastMove()
                    value = "${EMOJI_STONE[!draw.state.board.playerColor]}${lastPos}".asHighlightFormat()
                    inline = true
                }
            }
    }

    private fun InlineEmbed.buildResultFields(container: LanguageContainer, draw: BoardDraw, gameResult: GameResult) {
        field {
            name = container.boardMoves()
            value = draw.state.history.size.toString().asHighlightFormat()
            inline = true
        }

        field {
            name = container.boardResult()
            value = when (gameResult) {
                is GameResult.Win -> {
                    container.boardWinDescription(
                        "${gameResult.winner.name}${UNICODE_STONE[gameResult.winner]}".asHighlightFormat()
                    )
                }
                is GameResult.Full -> { container.boardTieDescription().asHighlightFormat() }
            }
            inline = true
        }
    }

    private fun buildGuideEmbed(session: GameSession, container: LanguageContainer): MessageEmbed? {
        val text = when {
            (session is PvpGameSession || session is EngineGameSession || session is MoveStageOpeningSession)
                    && session.gameResult == null ->
                container.boardCommandGuide()
            session is SwapStageOpeningSession -> session.offerCount
                ?.let { count -> container.boardStatefulSwapGuide(count) }
                ?: container.boardSwapGuide()
            session is BranchingStageOpeningSession -> container.boardBranchGuide()
            session is DeclareStageOpeningSession -> container.boardDeclareGuide()
            session is SelectStageOpeningSession -> container.boardSelectGuide()
            session is OfferStageOpeningSession -> container.boardOfferGuide(session.remainingMoves)
            else -> return null
        }

        return Embed {
            color = COLOR_GREEN_HEX
            description = text

            if (session is OpeningSession) {
                footer {
                    iconUrl = session.player.profileURL
                    name = "${session.player.name}'s turn."
                }
            }
        }
    }

    override fun buildBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, renderType: HistoryRenderType, draw: BoardDraw, session: GameSession?): DiscordMessageBuilder {
        val barColor =
            if (draw.result != null) COLOR_RED_HEX
            else COLOR_GREEN_HEX

        val modRenderType =
            if (draw.result != null) HistoryRenderType.SEQUENCE
            else renderType

        val offers = when (session) {
            is NegotiateStageOpeningSession -> session.moveCandidates
            else -> null
        }

        val blinds = when (session) {
            is MoveStageOpeningSession -> (0 until Pos.BOARD_SIZE).map { Pos.fromIdx(it) }
                .filterNot { session.isLegalMove(it) }
            is OfferStageOpeningSession -> (0 until Pos.BOARD_SIZE).map { Pos.fromIdx(it) }
                .filter {
                    session.state.board.stoneKind(it) == null
                            && session.state.board.forbiddenKind(it) == null
                            && it in session.symmetryMoves
                }
            else -> null
        }

        return renderer.renderBoard(draw.state, modRenderType, offers, blinds?.toSet()).fold(
            ifLeft = { textBoard ->
                publisher sends buildList {
                    add(Embed {
                        color = barColor

                        buildBoardAuthor(container, draw)
                        description = textBoard

                        draw.result
                            ?.let { buildResultFields(container, draw, it) }
                            ?: buildStatusFields(container, draw)
                    })

                    session
                        ?.let { buildGuideEmbed(it, container) }
                        ?.let { add(it) }
                }
            },
            ifRight = { imageStream ->
                val fName = ImageBoardRenderer.newFileName()

                val messageBuilder = publisher sends buildList {
                    add(Embed {
                        color = barColor

                        buildBoardAuthor(container, draw)

                        draw.result
                            ?.let { buildResultFields(container, draw, it) }
                            ?: buildStatusFields(container, draw)

                        image = "attachment://$fName"
                    })

                    if (session != null)
                        buildGuideEmbed(session, container)
                }

                messageBuilder.addFile(imageStream, fName)
            }
        )
    }

    fun buildReplayBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, renderType: HistoryRenderType, draw: BoardDraw): DiscordMessageBuilder =
        publisher sends buildList {
            renderer.renderBoard(draw.state, renderType, null, null).fold(
                ifLeft = { textBoard ->
                    add(Embed {
                        color = COLOR_NORMAL_HEX

                        buildBoardAuthor(container, draw)
                        description = textBoard
                    })
                },
                ifRight = { imageStream ->
                    val fName = ImageBoardRenderer.newFileName()

                    val messageBuilder = publisher sends buildList {
                        Embed {
                            color = COLOR_NORMAL_HEX

                            buildBoardAuthor(container, draw)

                            image = "attachment://$fName"
                        }
                    }

                    messageBuilder.addFile(imageStream, fName)
                }
            )
        }

    override fun buildSessionArchive(publisher: DiscordMessagePublisher, draw: BoardDraw): DiscordMessageBuilder {
        val imageStream = ImageBoardRenderer.renderInputStream(draw.state, HistoryRenderType.SEQUENCE, null, null, true)

        val fName = ImageBoardRenderer.newFileName()

        val messageBuilder = publisher sends Embed {
            color = COLOR_NORMAL_HEX

            buildBoardAuthor(Language.ENG.container, draw)

            draw.result
                ?.let { buildResultFields(Language.ENG.container, draw, it) }
                ?: buildStatusFields(Language.ENG.container, draw)

            image = "attachment://${fName}"
        }

        return messageBuilder.addFile(imageStream, fName)
    }

    override fun buildFocusedButtons(focusedFields: FocusedFields) =
        focusedFields.map { col -> ActionRow.of(
            col.map { (id, flag) -> when (flag) {
                ButtonFlag.EMPTY -> Button.of(ButtonStyle.SECONDARY, "${SET}-${id}", id)
                ButtonFlag.HIGHLIGHTED -> Button.of(ButtonStyle.PRIMARY, "${SET}-${id}", id)
                ButtonFlag.BLACK -> Button.of(ButtonStyle.SECONDARY, "${SET}-${id}", "", EMOJI_STONE[Color.BLACK]).asDisabled()
                ButtonFlag.WHITE -> Button.of(ButtonStyle.SECONDARY, "${SET}-${id}", "", EMOJI_STONE[Color.WHITE]).asDisabled()
                ButtonFlag.BLACK_RECENT -> Button.of(ButtonStyle.SUCCESS, "${SET}-${id}", "", EMOJI_STONE[Color.BLACK]).asDisabled()
                ButtonFlag.WHITE_RECENT -> Button.of(ButtonStyle.SUCCESS, "${SET}-${id}", "", EMOJI_STONE[Color.WHITE]).asDisabled()
                ButtonFlag.FORBIDDEN -> Button.of(ButtonStyle.DANGER, "${SET}-${id}", "", EMOJI_DARK_X).asDisabled()
                ButtonFlag.DISABLED -> Button.of(ButtonStyle.SECONDARY, "${SET}-${id}", id).asDisabled()
            } }
        ) }.reversed() // cartesian coordinate system

    override fun dispatchFocusButtons(publisher: DiscordComponentPublisher, focusedFields: FocusedFields): DiscordMessageBuilder =
        publisher(this.buildFocusedButtons(focusedFields))

    override fun buildSwapButtons(container: LanguageContainer): MessageComponents =
        listOf(
            ActionRow.of(
                Button.of(ButtonStyle.PRIMARY, "$OPENING-sy", container.swapSelectYes()),
                Button.of(ButtonStyle.SECONDARY, "$OPENING-sn", container.swapSelectNo())
            )
        )

    override fun buildBranchingButtons(container: LanguageContainer): MessageComponents =
        listOf(
            ActionRow.of(
                Button.of(ButtonStyle.PRIMARY, "$OPENING-bn", container.branchSelectSwap()),
                Button.of(ButtonStyle.SECONDARY, "$OPENING-by", container.branchSelectOffer())
            )
        )

    override fun buildDeclareButtons(container: LanguageContainer, session: DeclareStageOpeningSession) =
        StringSelectMenu(OPENING.toString()) {
            for (idx in 1 .. session.maxOfferCount) {
                option(
                    label = idx.toString(),
                    value = "$OPENING-d$idx",
                    default = false
                )
            }
        }.liftToButtons()

    override fun attachNavigators(flow: Flow<String>, message: SentMessage, checkTerminated: suspend () -> Boolean): Effect<Nothing, Unit> =
        effect {
            val discordMessage = message as? DiscordSentMessage ?: return@effect

            discordMessage.messageData.original
                ?.takeIf {
                    !it.isEphemeral
                            && ChannelManager.lookupPermission(it.guildChannel, Permission.MESSAGE_ADD_REACTION)
                            && ChannelManager.lookupPermission(it.guildChannel, Permission.MESSAGE_HISTORY)
                }
                ?.let { original ->
                    try {
                        coroutineScope {
                           flow
                                .collect { navigator ->
                                    when {
                                        checkTerminated() -> cancel()
                                        else -> {
                                            original
                                                .addReaction(Emoji.fromUnicode(navigator))
                                                .mapToResult()
                                                .await() // sync to JDA rate limiter
                                        }
                                    }
                                }
                        }
                    } catch (_: CancellationException) {
                    }
                }
        }

    // REPLAY

    override fun buildReplayButtons(gameRecordId: GameRecordId, validationKey: String, totalMoves: Int, currentMoves: Int): MessageComponents =
        listOf(when (currentMoves) {
            1 -> ActionRow.of(
                Button.of(ButtonStyle.SECONDARY, "$REPLAY_LIST-$validationKey-1", EMOJI_RETURN),
                Button.of(ButtonStyle.SECONDARY, "$NO_FEATURE-2", EMOJI_PREVIOUS).asDisabled(),
                Button.of(ButtonStyle.SECONDARY, "$NO_FEATURE-3", EMOJI_LEFT).asDisabled(),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-2-$validationKey-4", EMOJI_RIGHT),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-${totalMoves}-$validationKey-5", EMOJI_NEXT)
            )
            totalMoves -> ActionRow.of(
                Button.of(ButtonStyle.SECONDARY, "$REPLAY_LIST-$validationKey-1", EMOJI_RETURN),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-1-$validationKey-2", EMOJI_PREVIOUS),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-${currentMoves-1}-$validationKey-3", EMOJI_LEFT),
                Button.of(ButtonStyle.SECONDARY, "$NO_FEATURE-4", EMOJI_RIGHT).asDisabled(),
                Button.of(ButtonStyle.SECONDARY, "$NO_FEATURE-5", EMOJI_NEXT).asDisabled()
            )
            else -> ActionRow.of(
                Button.of(ButtonStyle.SECONDARY, "$REPLAY_LIST-$validationKey-1", EMOJI_RETURN),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-1-$validationKey-2", EMOJI_PREVIOUS),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-${currentMoves-1}-$validationKey-3", EMOJI_LEFT),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-${currentMoves+1}-$validationKey-4", EMOJI_RIGHT),
                Button.of(ButtonStyle.SECONDARY, "$REPLAY-${gameRecordId.id}-${totalMoves}-$validationKey-5", EMOJI_NEXT)
            )
        })

    override fun buildBackToListButton() =
        Button.of(ButtonStyle.SECONDARY, "$REPLAY_LIST", EMOJI_RETURN).liftToButtons()

    override fun buildReplayList(publisher: MessagePublisher, container: LanguageContainer, player: User.Human, records: List<GameRecord>): MessageBuilder {
        val embedBuilder = EmbedBuilder(
            color = COLOR_NORMAL_HEX
        )

        val selectMenuOptions = mutableListOf<SelectOption>()

        records.forEachIndexed { idx, record ->
            val userColor = record.users.map { it.id }.color(player.id)!!
            val opponent = record.users[!userColor]

            val result = when (record.gameResult.winner) {
                null -> "$UNICODE_PENCIL${container.replayEmbedDraw()}"
                userColor -> "$UNICODE_TROPHY${container.replayEmbedWin()}"
                else -> "$UNICODE_WHITE_FLAG${container.replayEmbedLose()}"
            }

            embedBuilder.field {
                name = "#${idx + 1}: ``${player.withColor(userColor)}`` vs ``${opponent.withColor(!userColor)}``, ``$result``"
                value = "``${record.date}``, ``${record.rule}``, ``${container.replayEmbedMatchInfo(record.history.size)}``"
                inline = false
            }

            selectMenuOptions.add(
                SelectOption.of(
                    "#${idx + 1}: ${player.withColor(userColor)} vs ${opponent.withColor(!userColor)}, $result",
                    "$REPLAY-${record.gameRecordId!!.id}-1-${player.id.validationKey}"
                )
            )
        }

        return (publisher sends embedBuilder.build())
            .addComponents(StringSelectMenu(REPLAY.toString(), options = selectMenuOptions).liftToButtons())
    }

    override fun buildReplay(publisher: MessagePublisher, container: LanguageContainer, gameRecord: GameRecord): MessageBuilder {
        val draw = GameRecordBoardDraw(gameRecord)

        val state = GameState(board = Board.fromHistory(gameRecord.history), gameRecord.history)

        val imageStream = ImageBoardRenderer.renderInputStream(state, HistoryRenderType.SEQUENCE, null, null, true)

        val fName = ImageBoardRenderer.newFileName()

        val messageBuilder = publisher sends Embed {
            color = COLOR_NORMAL_HEX

            buildBoardAuthor(Language.ENG.container, draw)
            buildResultFields(Language.ENG.container, draw, draw.result)

            image = "attachment://${fName}"
        }

        return messageBuilder.addFile(imageStream, fName)
    }

    // HELP

    private val aboutEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            title = container.helpAboutEmbedTitle()
            description = container.helpAboutEmbedDescription("Discord")
            thumbnail =
                "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/profile-thumbnail.jpg"

            field {
                name = container.helpAboutEmbedDeveloper()
                value = "@do1phin"
            }
            field {
                name = container.helpAboutEmbedRepository()
                value = "[github/GomokuBot](https://github.com/junghyun397/GomokuBot)"
            }
            field {
                name = container.helpAboutEmbedVersion()
                value = "3.0"
            }
            field {
                name = container.helpAboutEmbedSupport()
                value = "[discord/vq8pkfF](https://discord.gg/vq8pkfF)"
            }
            field {
                name = container.helpAboutEmbedInvite()
                value = "[discord/oauth2](https://discord.com/api/oauth2/authorize?client_id=452520939792498689&permissions=137439266880&scope=bot%20applications.commands)"
            }

            footer {
                name = "$UNICODE_ZAP Powered by Kotlin, Rust, Project Reactor, R2DBC, gRPC, JDA"
            }
        }
    }

    private val commandGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            title = container.commandUsageEmbedTitle()

            buildableCommands
                .flatMap { it.getLocalizedUsages(container) }
                .forEach { (usage, description) ->
                    field {
                        name = usage
                        value = description
                        inline = false
                    }
                }
        }
    }

    private val exploreAboutRenjuEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            description = container.exploreAboutRenju()
        }
    }

    private val buildAboutRenjuEmbed: (Pair<Int, LanguageContainer>) -> List<MessageEmbed> = memoize { (page, container) ->
        val (h2Title, h2Documents) = aboutRenjuDocument[container]!!.first[page]

        h2Documents
            .flatMapIndexed { h2Index, (h3Title, blocks) -> blocks
                .mapIndexed { blockIndex, block ->
                    Embed {
                        color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, page)
                        title = when {
                            h2Index == 0 && blockIndex == 0 -> h2Title.asBoldFormat()
                            blockIndex == 0 -> h3Title
                            else -> null
                        }

                        block.fold(
                            ifLeft = { image = it },
                            ifRight = { description = it }
                        )
                    }
                }
            }
    }

    private fun buildHelpMessage(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        when (page) {
            0 -> publisher sends listOf(
                this.aboutEmbed(container),
                this.commandGuideEmbed(container),
                this.exploreAboutRenjuEmbed(container)
            )
            else -> publisher sends this.buildAboutRenjuEmbed(tuple(page - 1, container))
        }

    override fun buildHelp(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        this.buildHelpMessage(publisher, container, page)

    override fun buildPaginatedHelp(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        this.buildHelpMessage(publisher, container, page)

    private fun buildSettingsMessage(publisher: DiscordMessagePublisher, config: ChannelConfig, page: Int) =
        when (page) {
            0 -> publisher(DiscordMessageData(embed = this.languageEmbed))
            1 -> publisher(DiscordMessageData(embed = this.settingEmbed(BoardStyle::class)(config.language.container)))
                .addComponents(this.settingMenu(BoardStyle::class)(config.language.container)(config).liftToButtons())
            2 -> publisher(DiscordMessageData(embed = this.settingEmbed(FocusType::class)(config.language.container)))
                .addComponents(this.settingMenu(FocusType::class)(config.language.container)(config).liftToButtons())
            3 -> publisher(DiscordMessageData(embed = this.settingEmbed(HintType::class)(config.language.container)))
                .addComponents(this.settingMenu(HintType::class)(config.language.container)(config).liftToButtons())
            4 -> publisher(DiscordMessageData(embed = this.settingEmbed(HistoryRenderType::class)(config.language.container)))
                .addComponents(this.settingMenu(HistoryRenderType::class)(config.language.container)(config).liftToButtons())
            5 -> publisher(DiscordMessageData(embed = this.settingEmbed(SwapType::class)(config.language.container)))
                .addComponents(this.settingMenu(SwapType::class)(config.language.container)(config).liftToButtons())
            6 -> publisher(DiscordMessageData(embed = this.settingEmbed(ArchivePolicy::class)(config.language.container)))
                .addComponents(this.settingMenu(ArchivePolicy::class)(config.language.container)(config).liftToButtons())
            else -> throw IllegalStateException()
        }

    override fun buildSettings(publisher: DiscordMessagePublisher, config: ChannelConfig, page: Int) =
        this.buildSettingsMessage(publisher, config, page)

    override fun buildPaginatedSettings(publisher: DiscordMessagePublisher, config: ChannelConfig, page: Int) =
        this.buildSettingsMessage(publisher, config, page)

    // RANK

    override fun buildRankings(publisher: DiscordMessagePublisher, container: LanguageContainer, rankings: List<Pair<User, UserStats>>) =
        publisher sends Embed {
            color = COLOR_NORMAL_HEX
            title = container.rankEmbedTitle()
            description = container.rankEmbedDescription()

            val win = container.rankEmbedWin()
            val lose = container.rankEmbedLose()
            val draw = container.rankEmbedDraw()

            rankings.forEachIndexed { index, details ->
                val (profile, stats) = details
                field {
                    name = "#${index + 1} ${profile.name}"
                    value = """
                            **$UNICODE_TROPHY$win: ``${stats.totalWins}``, $UNICODE_WHITE_FLAG️$lose: ``${stats.totalLosses}``, $UNICODE_PENCIL️$draw: ``${stats.totalDraws}``**
                            ``${UNICODE_STONE[Color.BLACK]}``$win: ``${stats.blackWins}``, ``${UNICODE_STONE[Color.BLACK]}``$lose: ``${stats.blackLosses}``,``${UNICODE_STONE[Color.BLACK]}``$draw: ``${stats.blackDraws}``
                            ``${UNICODE_STONE[Color.WHITE]}``$win: ``${stats.whiteWins}``, ``${UNICODE_STONE[Color.WHITE]}``$lose: ``${stats.whiteLosses}``,``${UNICODE_STONE[Color.WHITE]}``$draw: ``${stats.whiteDraws}``
                            """.trimIndent()
                    inline = false
                }
            }
        }

    // RATING

    override fun buildRating(publisher: DiscordMessagePublisher, container: LanguageContainer) = TODO()

    // LANG

    private val languageEmbed = Embed {
        color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.SETTINGS, 0)
        title = "GomokuBot / Language"
        description = "The default language is set based on the server region. Please apply the proper language for this server."

        Language.entries.forEach { language ->
            field {
                name = "${language.container.languageName()} (``${language.container.languageCode()}``)"
                value = language.container.languageSuggestion()
                inline = false
            }
        }
    }

    override fun buildLanguageGuide(publisher: DiscordMessagePublisher) =
        publisher sends languageEmbed

    // SETTINGS

    private val settingEmbed: (KClass<*>) -> (LanguageContainer) -> MessageEmbed =
        memoize { classTag -> memoize { container ->
            val (settingElement, optionElements) = SettingMapping.map[classTag]!!

            Embed {
                color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.SETTINGS, settingElement.menuIndex)
                title = "GomokuBot / ${settingElement.label(container)}"
                description = settingElement.description(container)

                optionElements.forEach { (_, optionElement) ->
                    field {
                        name = "${optionElement.emoji} ${optionElement.label(container)}"
                        value = optionElement.description(container)
                        inline = false
                    }
                }
            }
        } }

    private val settingMenu: (KClass<*>) -> (LanguageContainer) -> (ChannelConfig) -> SelectMenu =
        memoize { classTag -> memoize { container -> { config ->
            val (settingElement, optionElements) = SettingMapping.map[classTag]!!

            StringSelectMenu("p") {
                optionElements.forEach { (classTag, optionElement) ->
                    option(
                        label = optionElement.label(container),
                        value = optionElement.stringId,
                        emoji = Emoji.fromUnicode(optionElement.emoji),
                        default = settingElement.mapEnum(config) == classTag
                    )
                }
            }
        } } }

    // STYLE

    override fun buildStyleGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        publisher sends this.settingEmbed(BoardStyle::class)(container)

    // REQUEST

    override fun buildRequest(publisher: MessagePublisher, container: LanguageContainer, requester: User.Human, opponent: User.Human, rule: Rule) =
        publisher(DiscordMessageData(embed = Embed {
            color = COLOR_GREEN_HEX
            title = container.requestEmbedTitle()
            description = container.requestEmbedDescription(requester.asMentionFormat(), opponent.asMentionFormat())
        })).addComponents(
            listOf(ActionRow.of(
                Button.of(ButtonStyle.DANGER, "$REJECT-${rule.id}-${requester.givenId.idLong}", container.requestEmbedButtonReject()),
                Button.of(ButtonStyle.SUCCESS, "$ACCEPT-${rule.id}-${requester.givenId.idLong}", container.requestEmbedButtonAccept())
            ))
        )

    override fun buildRejectedRequest(publisher: DiscordMessagePublisher, container: LanguageContainer, requester: User, opponent: User) =
        publisher sends Embed {
            color = COLOR_RED_HEX
            title = "~~${container.requestEmbedTitle()}~~"
            description = "~~${container.requestEmbedDescription(requester.asMentionFormat(), opponent.asMentionFormat())}~~"
        }

    // UTILS

    override fun buildAnnounce(publisher: DiscordMessagePublisher, container: LanguageContainer, announce: Announce) =
        publisher sends Embed {
            color = COLOR_NORMAL_HEX
            title = "$UNICODE_SPEAKER ${announce.title}"
            description = announce.content

            footer {
                name = container.announceWrittenOn("$UNICODE_ALARM_CLOCK ${announce.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm"))} UTC")
            }
        }

    override fun buildSomethingWrongMessage(publisher: MessagePublisher, container: LanguageContainer, message: String) =
        publisher sends Embed {
            color = COLOR_RED_HEX
            title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
            description = message
            footer {
                name = "$UNICODE_MAILBOX ${container.notYetImplementedEmbedFooter()}"
            }
        }

    fun sendPermissionNotGrantedEmbed(publisher: (DiscordMessageData) -> MessageCreateAction, container: LanguageContainer, channelName: String) =
        publisher(DiscordMessageData(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
                description = container.permissionNotGrantedEmbedDescription(channelName.asHighlightFormat())
                footer {
                    name = "$UNICODE_ALARM_CLOCK ${container.permissionNotGrantedEmbedFooter()}"
                }
            }
        ))

}
