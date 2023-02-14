package discord.interact.message

import core.BotConfig
import core.assets.*
import core.database.entities.Announce
import core.database.entities.UserStats
import core.inference.FocusSolver
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.ButtonFlag
import core.interact.message.FocusedFields
import core.interact.message.MessageProducerImpl
import core.interact.message.SettingMapping
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.HistoryRenderType
import core.interact.message.graphics.ImageBoardRenderer
import core.session.*
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed
import discord.assets.EMOJI_BLACK_CIRCLE
import discord.assets.EMOJI_DARK_X
import discord.assets.EMOJI_WHITE_CIRCLE
import discord.interact.GuildManager
import discord.interact.parse.buildableCommands
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import renju.notation.Renju
import utils.assets.LinuxTime
import utils.assets.toBytes
import utils.lang.memoize
import utils.lang.pair
import utils.structs.*
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

object DiscordMessageProducer : MessageProducerImpl<DiscordMessageData, DiscordComponents>() {

    override val focusWidth = 5
    override val focusRange = 2 .. Renju.BOARD_WIDTH_MAX_IDX() - 2

    // INTERFACE

    override fun sendString(text: String, publisher: DiscordMessagePublisher) =
        publisher(DiscordMessageData(text))

    override fun User.asMentionFormat() = "<@${this.givenId.idLong}>"

    override fun String.asHighlightFormat() = "``$this``"

    override fun String.asBoldFormat() = "**$this**"

    // FORMAT

    private fun ItemComponent.liftToButtons() = listOf(ActionRow.of(this))

    fun encodePageNavigationState(base: Int, navigationState: PageNavigationState): Int =
        this.encodePageNavigationState(base, navigationState.kind, navigationState.page)

    private fun encodePageNavigationState(base: Int, kind: NavigationKind, page: Int): Int {
        val baseBytes = base.toBytes()
            .drop(1)
            .map { it.toUByte().toInt() }

        val headByte: Int = page shr 1
        val tailByte: Int = headByte + (headByte and 0x1)

        return ((baseBytes[0] + kind.id) shl 16) or ((baseBytes[1] + headByte) shl 8) or (baseBytes[2] + tailByte)
    }

    fun decodePageNavigationState(base: Int, code: Int, config: BotConfig, messageRef: MessageRef): Option<PageNavigationState> {
        val bytes = base.toBytes()
            .zip(code.toBytes()) { a, b -> b - a }
            .drop(1)

        val kind = NavigationKind.values().find(bytes.first().toShort())
        val page = bytes[1] + bytes[2]

        return Option.cond(kind != NavigationKind.BOARD && page in kind.range) {
            PageNavigationState(messageRef, kind, page, LinuxTime.nowWithOffset(config.navigatorExpireOffset))
        }
    }

    // BOARD

    private fun InlineEmbed.buildBoardAuthor(container: LanguageContainer, session: GameSession) =
        author {
            iconUrl = session.owner.profileURL
            name = buildString {
                append(session.ownerWithColor())
                append(" vs ")
                append(session.opponentWithColor())
                append(", ")

                if (session.gameResult.isDefined)
                    append(container.boardFinished())
                else
                    append(container.boardInProgress())
            }
        }

    private fun InlineEmbed.buildStatusFields(container: LanguageContainer, session: GameSession) =
        this.apply {
            session.board.lastPos().foreach {
                field {
                    name = container.boardMoves()
                    value = session.board.moves().toString().asHighlightFormat()
                    inline = true
                }

                field {
                    name = container.boardLastMove()
                    value = session.board.lastPos().get().toString().asHighlightFormat()
                    inline = true
                }
            }
        }

    private fun InlineEmbed.buildResultFields(container: LanguageContainer, session: GameSession, gameResult: GameResult) =
        this.apply {
            field {
                name = container.boardMoves()
                value = session.board.moves().toString().asHighlightFormat()
                inline = true
            }

            field {
                name = container.boardResult()
                value = when (gameResult) {
                    is GameResult.Win -> {
                        container.boardWinDescription(
                            "${gameResult.winner.name}${this@DiscordMessageProducer.unicodeStone(gameResult.winColor)}".asHighlightFormat()
                        )
                    }
                    is GameResult.Full -> { container.boardTieDescription().asHighlightFormat() }
                }
                inline = true
            }
        }

    private val buildNextMoveEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = COLOR_GREEN_HEX
            description = container.boardCommandGuide()
        }
    }

    override fun produceBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, renderType: HistoryRenderType, session: GameSession): DiscordMessageBuilder {
        val barColor = session.gameResult
            .fold(onDefined = { COLOR_RED_HEX }, onEmpty = { COLOR_GREEN_HEX })

        val modRenderType = session.gameResult
            .fold(onDefined = { HistoryRenderType.SEQUENCE }, onEmpty = { renderType })

        return renderer.renderBoard(session.board, session.history, modRenderType).fold(
            onLeft = { textBoard ->
                publisher(DiscordMessageData(
                    embeds = mutableListOf<MessageEmbed>().apply {
                        add(Embed {
                            color = barColor

                            buildBoardAuthor(container, session)
                            description = textBoard

                            session.gameResult.fold(
                                onDefined = { buildResultFields(container, session, it) },
                                onEmpty = { buildStatusFields(container, session) },
                            )
                        })

                        if (session.gameResult.isEmpty)
                            add(this@DiscordMessageProducer.buildNextMoveEmbed(container))
                    }
                ))
            },
            onRight = { imageStream ->
                val fName = ImageBoardRenderer.newFileName()

                publisher(DiscordMessageData(
                    embeds = mutableListOf<MessageEmbed>().apply {
                        add(Embed {
                            color = barColor

                            buildBoardAuthor(container, session)

                            session.gameResult.fold(
                                onDefined = { buildResultFields(container, session, it) },
                                onEmpty = { buildStatusFields(container, session) },
                            )

                            image = "attachment://$fName"
                        })

                        if (session.gameResult.isEmpty)
                            add(this@DiscordMessageProducer.buildNextMoveEmbed(container))
                    }
                )).addFile(imageStream, fName)
            }
        )
    }

    override fun produceSessionArchive(publisher: DiscordMessagePublisher, session: GameSession, result: Option<GameResult>, animate: Boolean): DiscordMessageBuilder {
        val imageStream = if (animate)
            ImageBoardRenderer.renderHistoryAnimation(session.history.filterNotNull())
        else
            ImageBoardRenderer.renderInputStream(session.board, session.history, HistoryRenderType.SEQUENCE, true)

        val fName = if (animate)
            ImageBoardRenderer.newGifFileName()
        else
            ImageBoardRenderer.newFileName()

        return publisher(DiscordMessageData(
            embed = Embed {
                color = COLOR_NORMAL_HEX

                buildBoardAuthor(Language.ENG.container, session)

                result.fold(
                    onDefined = { buildResultFields(Language.ENG.container, session, it) },
                    onEmpty = { buildStatusFields(Language.ENG.container, session) },
                )

                image = "attachment://${fName}"

            }
        )).addFile(imageStream, fName)
    }

    override fun generateFocusedButtons(focusedFields: FocusedFields) =
        focusedFields.map { col -> ActionRow.of(
            col.map { (id, flag) -> when (flag) {
                ButtonFlag.FREE -> Button.of(ButtonStyle.SECONDARY, "s-${id}", id)
                ButtonFlag.HIGHLIGHTED -> Button.of(ButtonStyle.PRIMARY, "s-${id}", id)
                ButtonFlag.BLACK -> Button.of(ButtonStyle.SECONDARY, "s-${id}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                ButtonFlag.WHITE -> Button.of(ButtonStyle.SECONDARY, "s-${id}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                ButtonFlag.BLACK_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${id}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                ButtonFlag.WHITE_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${id}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                ButtonFlag.FORBIDDEN -> Button.of(ButtonStyle.DANGER, "s-${id}", "", EMOJI_DARK_X).asDisabled()
            } }
        ) }.reversed() // cartesian coordinate system

    override fun attachFocusButtons(boardAction: DiscordMessageBuilder, session: GameSession, focusInfo: FocusSolver.FocusInfo): DiscordMessageBuilder =
        boardAction.addComponents(this.generateFocusedButtons(this.generateFocusedField(session.board, focusInfo)))

    override fun attachFocusButtons(publisher: DiscordComponentPublisher, session: GameSession, focusInfo: FocusSolver.FocusInfo): DiscordMessageBuilder =
        publisher(this.generateFocusedButtons(this.generateFocusedField(session.board, focusInfo)))

    override fun attachNavigators(flow: Flow<String>, message: DiscordMessageData, checkTerminated: suspend () -> Boolean) =
        IO { message.original
            .filter {
                !it.isEphemeral
                        && GuildManager.lookupPermission(it.guildChannel, Permission.MESSAGE_ADD_REACTION)
                        && GuildManager.lookupPermission(it.guildChannel, Permission.MESSAGE_HISTORY)
            }
            .forEach { original ->
                try {
                    coroutineScope {
                        flow
                            .map { original.addReaction(Emoji.fromUnicode(it)).mapToResult() }
                            .collect { action ->
                                when {
                                    checkTerminated() -> cancel()
                                    else -> {
                                        action.queue()
                                        delay(500) // TODO: sync on rate limit
                                    }
                                }
                            }
                    }
                }
                catch (_: CancellationException) {}
            }
        }

    // HELP

    private val buildAboutEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = encodePageNavigationState(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            title = container.helpAboutEmbedTitle()
            description = container.helpAboutEmbedDescription("Discord")
            thumbnail =
                "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/profile-thumbnail.jpg"

            field {
                name = container.helpAboutEmbedDeveloper()
                value = "junghyun397#6725"
            }
            field {
                name = container.helpAboutEmbedRepository()
                value = "[github/GomokuBot](https://github.com/junghyun397/GomokuBot)"
            }
            field {
                name = container.helpAboutEmbedVersion()
                value = "NG 1.2"
            }
            field {
                name = container.helpAboutEmbedSupport()
                value = "[discord.gg/vq8pkfF](https://discord.gg/vq8pkfF)"
            }
            field {
                name = container.helpAboutEmbedInvite()
                value = "[discord.com/api/oauth2](https://discord.com/api/oauth2/authorize?client_id=452520939792498689&permissions=137439266880&scope=bot%20applications.commands)"
            }

            footer {
                name = "$UNICODE_ZAP Powered by Kotlin, Scala, Project Reactor, R2DBC, gRPC, JDA"
            }
        }
    }

    private val buildCommandGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = encodePageNavigationState(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
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

    private val buildExploreAboutRenjuEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = encodePageNavigationState(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            description = container.exploreAboutRenju()
        }
    }

    private val buildAboutRenjuEmbed: (Pair<Int, LanguageContainer>) -> List<MessageEmbed> = memoize { (page, container) ->
        val (h2Title, h2Documents) = this.aboutRenjuDocument(container)[page]

        h2Documents
            .flatMapIndexed { h2Index, (h3Title, blocks) -> blocks
                .mapIndexed { blockIndex, block ->
                    Embed {
                        color = encodePageNavigationState(COLOR_NORMAL_HEX, NavigationKind.ABOUT, page)
                        title = when {
                            h2Index == 0 && blockIndex == 0 -> h2Title.asBoldFormat()
                            blockIndex == 0 -> h3Title
                            else -> null
                        }

                        block.fold(
                            onLeft = { description = it },
                            onRight = { image = it.ref }
                        )
                    }
                }
            }
    }

    private fun buildHelpMessage(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        when (page) {
            0 -> publisher(
                DiscordMessageData(
                    embeds = listOf(
                        this.buildAboutEmbed(container),
                        this.buildCommandGuideEmbed(container),
                        this.buildExploreAboutRenjuEmbed(container)
                    )
                )
            )
            else -> publisher(DiscordMessageData(embeds = this.buildAboutRenjuEmbed(page - 1 pair container)))
        }

    override fun produceHelp(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        this.buildHelpMessage(publisher, container, page)

    override fun paginateHelp(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        this.buildHelpMessage(publisher, container, page)

    private fun buildSettingsMessage(publisher: DiscordMessagePublisher, config: GuildConfig, page: Int) =
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

    override fun produceSettings(publisher: DiscordMessagePublisher, config: GuildConfig, page: Int) =
        this.buildSettingsMessage(publisher, config, page)

    override fun paginateSettings(publisher: DiscordMessagePublisher, config: GuildConfig, page: Int) =
        this.buildSettingsMessage(publisher, config, page)

    // RANK

    override fun produceRankings(publisher: DiscordMessagePublisher, container: LanguageContainer, rankings: List<Pair<User, UserStats>>) =
        publisher(DiscordMessageData(
            embed = Embed {
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
                                ``$UNICODE_BLACK_CIRCLE``$win: ``${stats.blackWins}``, ``$UNICODE_BLACK_CIRCLE``$lose: ``${stats.blackLosses}``,``$UNICODE_BLACK_CIRCLE``$draw: ``${stats.blackDraws}``
                                ``$UNICODE_WHITE_CIRCLE``$win: ``${stats.whiteWins}``, ``$UNICODE_WHITE_CIRCLE``$lose: ``${stats.whiteLosses}``,``$UNICODE_WHITE_CIRCLE``$draw: ``${stats.whiteDraws}``
                                """.trimIndent()
                        inline = false
                    }
                }
            }
        ))

    // RATING

    override fun produceRating(publisher: DiscordMessagePublisher, container: LanguageContainer) = TODO()

    // LANG

    private val languageEmbed = Embed {
        color = encodePageNavigationState(COLOR_NORMAL_HEX, NavigationKind.SETTINGS, 0)
        title = "GomokuBot / Language"
        description = "The default language is set based on the server region. Please apply the proper language for this server."

        Language.values().forEach { language ->
            field {
                name = "${language.container.languageName()} (``${language.container.languageCode()}``)"
                value = language.container.languageSuggestion()
                inline = false
            }
        }
    }

    override fun produceLanguageGuide(publisher: DiscordMessagePublisher) =
        publisher(DiscordMessageData(embed = languageEmbed))

    // SETTINGS

    private val settingEmbed: (KClass<*>) -> (LanguageContainer) -> MessageEmbed =
        memoize { classTag -> memoize { container ->
            val (settingElement, optionElements) = SettingMapping.map[classTag]!!

            Embed {
                color = encodePageNavigationState(COLOR_NORMAL_HEX, NavigationKind.SETTINGS, settingElement.menuIndex)
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

    private val settingMenu: (KClass<*>) -> (LanguageContainer) -> (GuildConfig) -> SelectMenu =
        memoize { classTag -> memoize { container -> { config ->
            val (settingElement, optionElements) = SettingMapping.map[classTag]!!

            StringSelectMenu("p") {
                optionElements.forEach { (classTag, optionElement) ->
                    option(
                        label = optionElement.label(container),
                        value = optionElement.stringId,
                        emoji = Emoji.fromUnicode(optionElement.emoji),
                        default = settingElement.extract(config) == classTag
                    )
                }
            }
        } } }

    // STYLE

    override fun produceStyleGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        publisher(DiscordMessageData(embed = this.settingEmbed(BoardStyle::class)(container)))

    // REQUEST

    override fun produceRequest(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher(DiscordMessageData(
            embed = Embed {
                color = COLOR_GREEN_HEX
                title = container.requestEmbedTitle()
                description = container.requestEmbedDescription(owner.asMentionFormat(), opponent.asMentionFormat())
            }
        )).addComponents(
            listOf(ActionRow.of(
                Button.of(ButtonStyle.DANGER, "r-${owner.givenId.idLong}", container.requestEmbedButtonReject()),
                Button.of(ButtonStyle.SUCCESS, "a-${owner.givenId.idLong}", container.requestEmbedButtonAccept())
            ))
        )

    override fun produceRequestInvalidated(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher(DiscordMessageData(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "~~${container.requestEmbedTitle()}~~"
                description = "~~${container.requestEmbedDescription(owner.asMentionFormat(), opponent.asMentionFormat())}~~"
            }
        ))

    // UTILS

    override fun produceAnnounce(publisher: DiscordMessagePublisher, container: LanguageContainer, announce: Announce) =
        publisher(DiscordMessageData(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = "$UNICODE_SPEAKER ${announce.title}"
                description = announce.content

                footer {
                    name = container.announceWrittenOn("$UNICODE_ALARM_CLOCK ${announce.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm"))} UTC")
                }
            }
        ))

    override fun produceNotYetImplemented(publisher: DiscordMessagePublisher, container: LanguageContainer, officialChannel: String) =
        publisher(DiscordMessageData(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
                description = container.notYetImplementedEmbedDescription()
                footer {
                    name = "$UNICODE_MAILBOX ${container.notYetImplementedEmbedFooter(officialChannel)}"
                }
            }
        ))

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
