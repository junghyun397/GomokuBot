package discord.interact.message

import core.assets.*
import core.database.entities.Announce
import core.database.entities.UserStats
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.*
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.ImageBoardRenderer
import core.session.*
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.Message
import discord.assets.*
import jrenju.notation.Pos
import jrenju.notation.Renju
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.RestAction
import utils.assets.toEnumString
import utils.lang.and
import utils.lang.memoize
import utils.structs.IO
import utils.structs.Option
import utils.structs.fold
import java.time.format.DateTimeFormatter

object DiscordMessageProducer : MessageProducerImpl<Message, DiscordComponents>() {

    override val focusWidth = 5
    override val focusRange = 2 .. Renju.BOARD_WIDTH_MAX_IDX() - 2

    // INTERFACE

    override fun sendString(text: String, publisher: MessagePublisher<Message, DiscordComponents>) =
        publisher(Message(text))

    override fun User.asMentionFormat() = "<@${this.givenId.idLong}>"

    override fun String.asHighlightFormat() = "``$this``"

    override fun String.asBoldFormat() = "**$this**"

    // FORMAT

    private fun ItemComponent.liftToButtons() = listOf(ActionRow.of(this))

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
            session.board.latestPos().foreach {
                field {
                    name = container.boardMoves()
                    value = session.board.moves().toString().asHighlightFormat()
                    inline = true
                }

                field {
                    name = container.boardLatestMove()
                    value = session.board.latestPos().get().toCartesian().asHighlightFormat()
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

    override fun produceBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): DiscordMessageIO {
        val barColor = session.gameResult.fold(onDefined = { COLOR_RED_HEX }, onEmpty = { COLOR_GREEN_HEX })
        return renderer.renderBoard(session.board, if (session.gameResult.isDefined) Option(session.history) else Option.Empty).fold(
            onLeft = { textBoard ->
                publisher(Message(
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
            onRight = { (imageStream, fName) ->
                publisher(Message(
                    embeds = mutableListOf<MessageEmbed>().apply {
                        add(Embed {
                            color = barColor

                            buildBoardAuthor(container, session)

                            session.gameResult.fold(
                                onDefined = { buildResultFields(container, session, it) },
                                onEmpty = { buildStatusFields(container, session) },
                            )

                            image = "attachment://${fName}"
                        })

                        if (session.gameResult.isEmpty)
                            add(this@DiscordMessageProducer.buildNextMoveEmbed(container))
                    }
                )).addFile(imageStream, fName)
            }
        )
    }

    override fun produceSessionArchive(publisher: DiscordMessagePublisher, session: GameSession, result: Option<GameResult>): DiscordMessageIO {
        val imageStream = ImageBoardRenderer.renderImageBoard(session.board, Option(session.history), true)
        val fName = ImageBoardRenderer.retrieveFileName()

        return publisher(Message(
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

    override fun attachFocusButtons(boardAction: DiscordMessageIO, session: GameSession, focus: Pos): DiscordMessageIO =
        boardAction.addButtons(this.generateFocusedButtons(this.generateFocusedField(session.board, focus)))

    override fun attachFocusButtons(publisher: ComponentPublisher<Message, DiscordComponents>, session: GameSession, focus: Pos) =
        publisher(this.generateFocusedButtons(this.generateFocusedField(session.board, focus)))

    override fun attachNavigators(flow: Flow<String>, message: MessageAdaptor<Message, DiscordComponents>, checkTerminated: suspend () -> Boolean) =
        IO {
            if (message.original.isEphemeral) return@IO

            try {
                coroutineScope {
                    flow
                        .map { message.original.addReaction(Emoji.fromUnicode(it)).mapToResult() }
                        .collect { action ->
                            when {
                                checkTerminated() -> cancel()
                                else -> {
                                    action.queue()
                                    delay(500) // sync on rate limit?
                                }
                            }
                        }
                }
            }
            catch (_: CancellationException) {}
        }

    // HELP

    private val buildAboutEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = COLOR_NORMAL_HEX
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
                value = "NG 1.0"
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
            color = COLOR_NORMAL_HEX
            title = container.helpCommandEmbedTitle()

            field {
                name = "``/${container.helpCommand()}`` or ``$COMMAND_PREFIX${container.helpCommand()}``"
                value = container.helpCommandEmbedHelp()
                inline = false
            }

            field {
                name = "``/${container.settingsCommand()}`` or ``$COMMAND_PREFIX${container.settingsCommand()}``"
                value = container.helpCommandEmbedSettings()
                inline = false
            }

            field {
                name = "``/${container.languageCommand()}`` or ``$COMMAND_PREFIX${container.languageCommand()}``"
                value = container.helpCommandEmbedLang(languageList)
                inline = false
            }

            field {
                name = "``/${container.styleCommand()}`` or ``$COMMAND_PREFIX${container.styleCommand()}``"
                value = container.helpCommandEmbedStyle()
                inline = false
            }

            field {
                name = "``/${container.startCommand()}`` or ``$COMMAND_PREFIX${container.startCommand()}``"
                value = container.helpCommandEmbedStartPVE()
                inline = false
            }

            field {
                name = "``/${container.startCommand()} @mention`` or ``$COMMAND_PREFIX${container.startCommand()} @mention``"
                value = container.helpCommandEmbedStartPVP()
                inline = false
            }

            field {
                name = "``/${container.resignCommand()}`` or ``$COMMAND_PREFIX${container.resignCommand()}``"
                value = container.helpCommandEmbedResign()
                inline = false
            }

            field {
                name = "``/${container.rankCommand()}`` or ``$COMMAND_PREFIX${container.rankCommand()}``"
                value = container.helpCommandEmbedRank()
                inline = false
            }

            field {
                name = "``/${container.ratingCommand()}`` or ``$COMMAND_PREFIX${container.ratingCommand()}``"
                value = container.helpCommandEmbedRating()
                inline = false
            }
        }
    }

    private val buildExploreAboutRenjuEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            color = COLOR_NORMAL_HEX
            description = container.exploreAboutRenju()
        }
    }

    private val buildAboutRenjuEmbed: (Pair<Int, LanguageContainer>) -> List<MessageEmbed> = memoize { (page, container) ->
        val (h2Title, h2Documents) = this.aboutRenjuDocument(container)[page]

        h2Documents
            .flatMapIndexed { h2Index, (h3Title, blocks) -> blocks
                .mapIndexed { blockIndex, block ->
                    Embed {
                        color = COLOR_NORMAL_HEX
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
            0 -> publisher(Message(embeds = listOf(
                this.buildAboutEmbed(container),
                this.buildCommandGuideEmbed(container),
                this.buildExploreAboutRenjuEmbed(container)
            )))
            else -> publisher(Message(embeds = this.buildAboutRenjuEmbed(page - 1 and container)))
        }

    override fun produceHelp(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        this.buildHelpMessage(publisher, container, page)

    override fun paginateHelp(publisher: MessagePublisher<Message, DiscordComponents>, container: LanguageContainer, page: Int) =
        this.buildHelpMessage(publisher, container, page)

    private fun buildSettingsMessage(publisher: DiscordMessagePublisher, config: GuildConfig, page: Int) =
        when (page) {
            0 -> publisher(Message(embed = this.languageEmbed))
            1 -> publisher(Message(embed = this.buildStyleGuideEmbed(config.language.container)))
                .addButtons(this.buildStylePolicyMenu(config).liftToButtons())
            2 -> publisher(Message(embed = this.buildFocusPolicyGuideEmbed(config.language.container)))
                .addButtons(this.buildFocusPolicyMenu(config).liftToButtons())
            3 -> publisher(Message(embed = this.buildSweepPolicyGuideEmbed(config.language.container)))
                .addButtons(this.buildSweepPolicyMenu(config).liftToButtons())
            4 -> publisher(Message(embed = this.buildArchivePolicyGuideEmbed(config.language.container)))
                .addButtons(this.buildArchivePolicyMenu(config).liftToButtons())
            else -> throw IllegalStateException()
        }

    override fun produceSettings(publisher: MessagePublisher<Message, DiscordComponents>, config: GuildConfig, page: Int) =
        this.buildSettingsMessage(publisher, config, page)

    override fun paginateSettings(publisher: MessagePublisher<Message, DiscordComponents>, config: GuildConfig, page: Int) =
        this.buildSettingsMessage(publisher, config, page)

    // RANK

    override fun produceRankings(publisher: MessagePublisher<Message, DiscordComponents>, container: LanguageContainer, rankings: List<Pair<User, UserStats>>) =
        publisher(Message(
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
        color = COLOR_NORMAL_HEX
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
        publisher(Message(embed = languageEmbed))

    // SETTINGS

    private val buildStyleGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            title = container.styleEmbedTitle()
            description = container.styleEmbedDescription()
            color = COLOR_NORMAL_HEX

            field {
                name = "$UNICODE_IMAGE ${container.styleSelectImage()} (``${BoardStyle.IMAGE.sample.styleShortcut}``)"
                value = container.styleSelectImageDescription()
                inline = false
            }

            field {
                name = "$UNICODE_T ${container.styleSelectText()} (``${BoardStyle.TEXT.sample.styleShortcut}``)"
                value = container.styleSelectTextDescription()
                inline = false
            }

            field {
                name = "$UNICODE_T ${container.styleSelectSolidText()} (``${BoardStyle.SOLID_TEXT.sample.styleShortcut}``)"
                value = container.styleSelectSolidTextDescription()
                inline = false
            }

            field {
                name = "$UNICODE_GEM ${container.styleSelectUnicodeText()} (``${BoardStyle.UNICODE.sample.styleShortcut}``)"
                value = container.styleSelectUnicodeTextDescription()
                inline = false
            }
        }
    }

    private fun buildStylePolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.styleSelectImage(),
                value = BoardStyle.IMAGE.toEnumString(),
                emoji = EMOJI_IMAGE,
                default = config.boardStyle == BoardStyle.IMAGE
            )
            option(
                label = config.language.container.styleSelectText(),
                value = BoardStyle.TEXT.toEnumString(),
                emoji = EMOJI_T,
                default = config.boardStyle == BoardStyle.TEXT
            )
            option(
                label = config.language.container.styleSelectSolidText(),
                value = BoardStyle.SOLID_TEXT.toEnumString(),
                emoji = EMOJI_T,
                default = config.boardStyle == BoardStyle.SOLID_TEXT
            )
            option(
                label = config.language.container.styleSelectUnicodeText(),
                value = BoardStyle.UNICODE.toEnumString(),
                emoji = EMOJI_GEM,
                default = config.boardStyle == BoardStyle.UNICODE
            )
        }

    private val buildFocusPolicyGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            title = container.focusEmbedTitle()
            description = container.focusEmbedDescription()
            color = COLOR_NORMAL_HEX

            field {
                name = "$UNICODE_ZAP ${container.focusSelectIntelligence()}"
                value = container.focusSelectIntelligenceDescription()
                inline = false
            }

            field {
                name = "$UNICODE_MAG ${container.focusSelectFallowing()}"
                value = container.focusSelectFallowingDescription()
                inline = false
            }
        }
    }

    private fun buildFocusPolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.focusSelectIntelligence(),
                value = FocusPolicy.INTELLIGENCE.toEnumString(),
                emoji = EMOJI_ZAP,
                default = config.focusPolicy == FocusPolicy.INTELLIGENCE
            )
            option(
                label = config.language.container.focusSelectFallowing(),
                value = FocusPolicy.FALLOWING.toEnumString(),
                emoji = EMOJI_MAG,
                default = config.focusPolicy == FocusPolicy.FALLOWING
            )
        }

    private val buildSweepPolicyGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            title = container.sweepEmbedTitle()
            description = container.sweepEmbedDescription()
            color = COLOR_NORMAL_HEX

            field {
                name = "$UNICODE_BROOM ${container.sweepSelectRelay()}"
                value = container.sweepSelectRelayDescription()
                inline = false
            }

            field {
                name = "$UNICODE_CABINET ${container.sweepSelectLeave()}"
                value = container.sweepSelectLeaveDescription()
                inline = false
            }
        }
    }

    private fun buildSweepPolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.sweepSelectRelay(),
                value = SweepPolicy.RELAY.toEnumString(),
                emoji = EMOJI_BROOM,
                default = config.sweepPolicy == SweepPolicy.RELAY
            )
            option(
                label = config.language.container.sweepSelectLeave(),
                value = SweepPolicy.LEAVE.toEnumString(),
                emoji = EMOJI_CABINET,
                default = config.sweepPolicy == SweepPolicy.LEAVE
            )
        }

    private val buildArchivePolicyGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            title = container.archiveEmbedTitle()
            description = container.archiveEmbedDescription()
            color = COLOR_NORMAL_HEX

            field {
                name = "$UNICODE_SILHOUETTE ${container.archiveSelectByAnonymous()}"
                value = container.archiveSelectByAnonymousDescription()
                inline = false
            }

            field {
                name = "$UNICODE_SMILING ${container.archiveSelectWithProfile()}"
                value = container.archiveSelectWithProfileDescription()
                inline = false
            }

            field {
                name = "$UNICODE_LOCK ${container.archiveSelectPrivacy()}"
                value = container.archiveSelectPrivacyDescription()
                inline = false
            }
        }
    }

    private fun buildArchivePolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.archiveSelectByAnonymous(),
                value = ArchivePolicy.BY_ANONYMOUS.toEnumString(),
                emoji = EMOJI_SILHOUETTE,
                default = config.archivePolicy == ArchivePolicy.BY_ANONYMOUS
            )
            option(
                label = config.language.container.archiveSelectWithProfile(),
                value = ArchivePolicy.WITH_PROFILE.toEnumString(),
                emoji = EMOJI_SMILING,
                default = config.archivePolicy == ArchivePolicy.WITH_PROFILE
            )
            option(
                label = config.language.container.archiveSelectPrivacy(),
                value = ArchivePolicy.PRIVACY.toEnumString(),
                emoji = EMOJI_LOCK,
                default = config.archivePolicy == ArchivePolicy.PRIVACY
            )
        }

    // STYLE

    override fun produceStyleGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        publisher(Message(embed = this.buildStyleGuideEmbed(container)))

    // REQUEST

    override fun produceRequest(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher(Message(
            embed = Embed {
                color = COLOR_GREEN_HEX
                title = container.requestEmbedTitle()
                description = container.requestEmbedDescription(owner.asMentionFormat(), opponent.asMentionFormat())
            }
        )).addButtons(
            listOf(ActionRow.of(
                Button.of(ButtonStyle.DANGER, "r-${owner.givenId.idLong}", container.requestEmbedButtonReject()),
                Button.of(ButtonStyle.SUCCESS, "a-${owner.givenId.idLong}", container.requestEmbedButtonAccept())
            ))
        )

    override fun produceRequestInvalidated(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "~~${container.requestEmbedTitle()}~~"
                description = "~~${container.requestEmbedDescription(owner.asMentionFormat(), opponent.asMentionFormat())}~~"
            }
        ))

    // UTILS

    override fun produceAnnounce(publisher: MessagePublisher<Message, DiscordComponents>, container: LanguageContainer, announce: Announce) =
        publisher(Message(
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
        publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
                description = container.notYetImplementedEmbedDescription()
                footer {
                    name = "$UNICODE_MAILBOX ${container.notYetImplementedEmbedFooter(officialChannel)}"
                }
            }
        ))

    fun sendPermissionNotGrantedEmbed(publisher: (Message) -> RestAction<Message>, container: LanguageContainer, channelName: String) =
        publisher(Message(
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
