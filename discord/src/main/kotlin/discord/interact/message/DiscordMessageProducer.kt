package discord.interact.message

import core.assets.*
import core.database.entities.SimpleProfile
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.*
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.BoardStyle
import core.interact.message.graphics.ImageBoardRenderer
import core.session.ArchivePolicy
import core.session.FocusPolicy
import core.session.GameResult
import core.session.SweepPolicy
import core.session.entities.GameSession
import core.session.entities.GuildConfig
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.InlineEmbed
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
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
import net.dv8tion.jda.api.exceptions.ContextException
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.RestAction
import utils.assets.toEnumString
import utils.lang.memoize
import utils.structs.IO
import utils.structs.Option

object DiscordMessageProducer : MessageProducer<Message, DiscordButtons>() {

    override val focusWidth = 5
    override val focusRange = 2 .. Renju.BOARD_WIDTH_MAX_IDX() - 2

    private fun User.asMentionFormat() = "<@${this.id.idLong}>"

    private fun String.asHighlightFormat() = "``$this``"

    private fun ItemComponent.liftToButtons() = listOf(ActionRow.of(this))

    // BOARD

    private fun InlineEmbed.buildBoardAuthor(container: LanguageContainer, session: GameSession) =
        this.author {
            iconUrl = session.owner.profileURL
            name = StringBuilder().apply {
                append(session.ownerWithColor())
                append(" vs ")
                append(session.opponentWithColor())
                append(", ")

                if (session.gameResult.isDefined)
                    append(container.boardFinished())
                else
                    append(container.boardInProgress())
            }.toString()
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

    override fun produceBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<DiscordMessageAction> {
        val barColor = session.gameResult.fold(onDefined = { COLOR_RED_HEX }, onEmpty = { COLOR_GREEN_HEX })
        return renderer.renderBoard(session.board, if (session.gameResult.isDefined) Option(session.history) else Option.Empty).fold(
            onLeft = { textBoard -> IO {
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
            } },
            onRight = { (imageStream, fName) -> IO {
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
            } }
        )
    }

    override fun produceSessionArchive(publisher: DiscordMessagePublisher, session: GameSession): IO<DiscordMessageAction> {
        val (imageStream, fName) = ImageBoardRenderer.renderImageBoard(session.board, Option(session.history))
        return IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX

                buildBoardAuthor(Language.ENG.container, session)

                session.gameResult.fold(
                    onDefined = { buildResultFields(Language.ENG.container, session, it) },
                    onEmpty = { buildStatusFields(Language.ENG.container, session) },
                )

                image = "attachment://${fName}"

            }
        )).addFile(imageStream, fName) }
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

    override fun attachFocusButtons(boardAction: DiscordMessageAction, session: GameSession, focus: Pos): DiscordMessageAction =
        boardAction.addButtons(this.generateFocusedButtons(this.generateFocusedField(session.board, focus)))

    override fun attachNavigators(flow: Flow<String>, message: MessageAdaptor<Message, DiscordButtons>, checkTerminated: suspend () -> Boolean) =
        IO {
            try {
                coroutineScope {
                    flow
                        .map { message.original.addReaction(it) }
                        .collect {
                            if (checkTerminated())
                                this@coroutineScope.cancel()
                            else {
                                try { it.queue() }
                                catch (_: ErrorResponseException) { this@coroutineScope.cancel() }
                                catch (_: ContextException) { this@coroutineScope.cancel() } // JDA bug?
                                delay(500)
                            }
                        }
                }
            }
            catch (_: CancellationException) {}
        }

    // GAME

    override fun produceBeginsPVP(publisher: DiscordMessagePublisher, container: LanguageContainer, blackPlayer: User, whitePlayer: User): IO<MessageIO<Message, DiscordButtons>> =
        IO { publisher(Message(container.beginPVP(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat()))) }

    override fun produceBeginsPVE(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, ownerHasBlack: Boolean) =
        IO { publisher(Message(
            if (ownerHasBlack)
                container.beginPVEAiWhite(owner.asMentionFormat())
            else
                container.beginPVEAiBlack(owner.asMentionFormat())
        )) }

    override fun produceNextMovePVP(publisher: DiscordMessagePublisher, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos) =
        IO { publisher(Message(container.processNextPVP(previousPlayer.asMentionFormat(), nextPlayer.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()))) }

    override fun produceWinPVP(publisher: DiscordMessagePublisher, container: LanguageContainer, winner: User, looser: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVPWin(winner.asMentionFormat(), looser.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())))}

    override fun produceTiePVP(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceSurrenderedPVP(publisher: DiscordMessagePublisher, container: LanguageContainer, winner: User, looser: User) =
        IO { publisher(Message(container.endPVPResign(winner.asMentionFormat(), looser.asMentionFormat()))) }

    override fun produceTimeoutPVP(publisher: DiscordMessagePublisher, container: LanguageContainer, winner: User, looser: User) =
        IO { publisher(Message(container.endPVPTimeOut(winner.asMentionFormat(), looser.asMentionFormat()))) }

    override fun produceNextMovePVE(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.processNextPVE(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()))) }

    override fun produceWinPVE(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVEWin(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()))) }

    override fun produceLosePVE(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVELose(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())))}

    override fun produceTiePVE(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.endPVETie(owner.asMentionFormat()))) }

    override fun produceSurrenderedPVE(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.endPVEResign(owner.asMentionFormat())))}

    override fun produceTimeoutPVE(publisher: DiscordMessagePublisher, container: LanguageContainer, player: User) =
        IO { publisher(Message(container.endPVETimeOut(player.asMentionFormat()))) }

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
                value = "[do1ph.in/gomokubot](https://do1ph.in/gomokubot)"
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
                name = "``/${container.configCommand()}`` or ``$COMMAND_PREFIX${container.configCommand()}``"
                value = container.helpCommandEmbedConfig()
                inline = false
            }

            field {
                name = "``/${container.languageCommand()}`` or ``$COMMAND_PREFIX${container.languageCommand()}``"
                value = container.helpCommandEmbedLang(languageEnumeration)
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

    private fun buildHelpMessage(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        when (page) {
            0 -> publisher(Message(embeds = listOf(this.buildAboutEmbed(container), this.buildCommandGuideEmbed(container))))
            else -> throw IllegalStateException()
        }

    override fun produceHelp(publisher: DiscordMessagePublisher, container: LanguageContainer, page: Int) =
        IO { publisher(Message(embeds = listOf(this.buildAboutEmbed(container), this.buildCommandGuideEmbed(container)))) }

    override fun paginateHelp(original: MessageAdaptor<Message, DiscordButtons>, container: LanguageContainer, page: Int) =
        IO { this.buildHelpMessage({ MessageActionAdaptor(original.original.editMessage(it)) }, container, page)}

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

    override fun produceSettings(publisher: MessagePublisher<Message, DiscordButtons>, config: GuildConfig, page: Int) =
        IO { this.buildSettingsMessage(publisher, config, page) }

    override fun paginateSettings(original: MessageAdaptor<Message, DiscordButtons>, config: GuildConfig, page: Int) =
        IO {
            val message = original.original
                .editMessageComponents()
                .await()

            this.buildSettingsMessage({ MessageActionAdaptor(message.editMessage(it)) }, config, page)
        }

    // RANK

    override fun produceRankings(publisher: DiscordMessagePublisher, container: LanguageContainer, rankings: List<SimpleProfile>) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.rankEmbedTitle()
                description = container.rankEmbedDescription()

                rankings.forEachIndexed { index, simpleProfile ->
                    field {
                        name = "#${index + 1} ${simpleProfile.name}"
                        value = "${container.rankEmbedWin()}: ``${simpleProfile.wins}``, ${container.rankEmbedLose()}: ``${simpleProfile.losses}``"
                        inline = false
                    }
                }
            }
        )) }

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
        IO { publisher(Message(embed = languageEmbed)) }

    override fun produceLanguageNotFound(publisher: DiscordMessagePublisher) =
        IO { publisher(Message(content = "There is an error in the Language Code. Please select from the llist below.")) }

    override fun produceLanguageUpdated(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(content = container.languageUpdated())) }

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
        IO { publisher(Message(embed = this.buildStyleGuideEmbed(container))) }

    override fun produceStyleNotFound(publisher: DiscordMessagePublisher, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.styleErrorNotfound(user.asMentionFormat()))) }

    override fun produceStyleUpdated(publisher: DiscordMessagePublisher, container: LanguageContainer, style: String) =
        IO { publisher(Message(content = container.styleUpdated(style))) }

    // CONFIG

    override fun produceConfigApplied(publisher: DiscordMessagePublisher, container: LanguageContainer, configKind: String, configChoice: String) =
        IO { publisher(Message(content = container.configApplied(configChoice.asHighlightFormat()))) }

    // SESSION

    override fun produceSessionNotFound(publisher: DiscordMessagePublisher, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.sessionNotFound(user.asMentionFormat()))) }

    // START

    override fun produceSessionAlready(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User) =
        IO { publisher(Message(content = container.startErrorSessionAlready(owner.asMentionFormat()))) }

    override fun produceOpponentSessionAlready(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorOpponentSessionAlready(owner.asMentionFormat(), opponent.asMentionFormat())))}

    override fun produceRequestAlreadySent(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorRequestAlreadySent(owner.asMentionFormat(), opponent.asMentionFormat())))}

    override fun produceRequestAlready(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorRequestAlready(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceOpponentRequestAlready(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorOpponentRequestAlready(owner.asMentionFormat(), opponent.asMentionFormat())))}

    // SET

    override fun produceOrderFailure(publisher: DiscordMessagePublisher, container: LanguageContainer, user: User, player: User) =
        IO { publisher(Message(content = container.processErrorOrder(user.asMentionFormat(), player.asMentionFormat()))) }

    override fun produceSetIllegalArgument(publisher: DiscordMessagePublisher, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.setErrorIllegalArgument(user.asMentionFormat()))) }

    override fun produceSetAlreadyExist(publisher: DiscordMessagePublisher, container: LanguageContainer, user: User, pos: Pos) =
        IO { publisher(Message(content = container.setErrorExist(user.asMentionFormat(), pos.toCartesian().asHighlightFormat()))) }

    override fun produceSetForbiddenMove(publisher: DiscordMessagePublisher, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte) =
        IO { publisher(Message(content = container.setErrorForbidden(user.asMentionFormat(), pos.toCartesian().asHighlightFormat(), forbiddenFlagToText(forbiddenFlag).asHighlightFormat()))) }

    // REQUEST

    override fun produceRequest(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_GREEN_HEX
                title = container.requestEmbedTitle()
                description = container.requestEmbedDescription(owner.asMentionFormat(), opponent.asMentionFormat())
            }
        )).addButtons(
            listOf(ActionRow.of(
                Button.of(ButtonStyle.DANGER, "r-${owner.id.idLong}", container.requestEmbedButtonReject()),
                Button.of(ButtonStyle.SUCCESS, "a-${owner.id.idLong}", container.requestEmbedButtonAccept())
            ))
        ) }

    override fun produceRequestRejected(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.requestRejected(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceRequestExpired(publisher: DiscordMessagePublisher, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.requestExpired(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    // UTILS

    override fun produceNotYetImplemented(publisher: DiscordMessagePublisher, container: LanguageContainer, officialChannel: String) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
                description = container.notYetImplementedEmbedDescription()
                footer {
                    name = "$UNICODE_MAILBOX ${container.notYetImplementedEmbedFooter(officialChannel)}"

                }
            }
        )) }

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
