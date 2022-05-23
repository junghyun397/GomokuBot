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
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.RestAction
import utils.structs.IO
import utils.structs.Option

object DiscordMessageProducer : MessageProducer<Message, DiscordButtons>() {

    override val focusWidth = 5
    override val focusRange = 2 until Renju.BOARD_WIDTH_MAX_IDX() - 2

    private fun User.asMentionFormat() = "<@${this.id.idLong}>"

    private fun String.asHighlightFormat() = "``$this``"

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

    private fun buildNextMoveEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_GREEN_HEX
            description = container.boardCommandGuide()
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
            onRight = { imageBoard -> IO {
                publisher(Message(
                    embeds = mutableListOf<MessageEmbed>().apply {
                        add(Embed {
                            color = barColor

                            buildBoardAuthor(container, session)

                            session.gameResult.fold(
                                onDefined = { buildResultFields(container, session, it) },
                                onEmpty = { buildStatusFields(container, session) },
                            )

                            image = "attachment://${imageBoard.second}"
                        })

                        if (session.gameResult.isEmpty)
                            add(this@DiscordMessageProducer.buildNextMoveEmbed(container))
                    }
                )).addFile(imageBoard.first, imageBoard.second)
            } }
        )
    }

    override fun produceSessionArchive(publisher: MessagePublisher<Message, DiscordButtons>, session: GameSession): IO<DiscordMessageAction> {
        val board = ImageBoardRenderer.renderBoardImage(session.board, Option(session.history))
        return IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX

                buildBoardAuthor(Language.ENG.container, session)

                session.gameResult.fold(
                    onDefined = { buildResultFields(Language.ENG.container, session, it) },
                    onEmpty = { buildStatusFields(Language.ENG.container, session) },
                )

                image = "attachment://${board.second}"

            }
        )).addFile(board.first, board.second) }
    }

    override fun generateFocusedButtons(focusedFields: FocusedFields) =
        focusedFields.map { col -> ActionRow.of(
            col.map { element -> when (element.second) {
                ButtonFlag.FREE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", element.first)
                ButtonFlag.BLACK -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                ButtonFlag.WHITE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                ButtonFlag.BLACK_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                ButtonFlag.WHITE_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                ButtonFlag.FORBIDDEN -> Button.of(ButtonStyle.DANGER, "s-${element.first}", "", EMOJI_DARK_X).asDisabled()
                ButtonFlag.HIGHLIGHTED -> Button.of(ButtonStyle.PRIMARY, "s-${element.first}", element.first)
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
                                it.queue()
                                delay(500)
                            }
                        }
                }
            } catch (_: CancellationException) {}
        }

    // GAME

    override fun produceBeginsPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, blackPlayer: User, whitePlayer: User): IO<MessageIO<Message, DiscordButtons>> =
        IO { publisher(Message(container.beginPVP(blackPlayer.asMentionFormat(), whitePlayer.asMentionFormat()))) }

    override fun produceBeginsPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, ownerHasBlack: Boolean) =
        IO { publisher(Message(
            if (ownerHasBlack)
                container.beginPVEAiWhite(owner.asMentionFormat())
            else
                container.beginPVEAiBlack(owner.asMentionFormat())
        ))}

    override fun produceNextMovePVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos) =
        IO { publisher(Message(container.processNextPVP(previousPlayer.asMentionFormat(), nextPlayer.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()))) }

    override fun produceWinPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVPWin(winner.asMentionFormat(), looser.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())))}

    override fun produceTiePVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceSurrenderedPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User) =
        IO { publisher(Message(container.endPVPResign(winner.asMentionFormat(), looser.asMentionFormat()))) }

    override fun produceTimeoutPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User) =
        IO { publisher(Message(container.endPVPTimeOut(winner.asMentionFormat(), looser.asMentionFormat()))) }

    override fun produceNextMovePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.processNextPVE(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()))) }

    override fun produceWinPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVEWin(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat()))) }

    override fun produceLosePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVELose(owner.asMentionFormat(), latestMove.toCartesian().asHighlightFormat())))}

    override fun produceTiePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.endPVETie(owner.asMentionFormat()))) }

    override fun produceSurrenderedPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.endPVEResign(owner.asMentionFormat())))}

    override fun produceTimeoutPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, player: User) =
        IO { publisher(Message(container.endPVETimeOut(player.asMentionFormat()))) }

    // HELP

    private fun buildAboutEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_NORMAL_HEX
            title = container.helpAboutEmbedTitle()
            description = container.helpAboutEmbedDescription("Discord")
            thumbnail = "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/profile-thumbnail.jpg"

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

    private fun buildCommandGuideEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_NORMAL_HEX
            title = container.helpCommandEmbedTitle()
            field {
                name = "``/${container.helpCommand()}`` or ``~${container.helpCommand()}``"
                value = container.helpCommandEmbedHelp()
                inline = false
            }
            field {
                name = "``/${container.startCommand()}`` or ``~${container.startCommand()}``"
                value = container.helpCommandEmbedStartPVE()
                inline = false
            }
            field {
                name = "``/${container.startCommand()} @mention`` or ``~${container.startCommand()} @mention``"
                value = container.helpCommandEmbedStartPVP()
                inline = false
            }
            field {
                name = "``/${container.resignCommand()}`` or ``~${container.resignCommand()}``"
                value = container.helpCommandEmbedResign()
                inline = false
            }
            field {
                name = "``/${container.languageCommand()}`` or ``~${container.languageCommand()}``"
                value = container.helpCommandEmbedLang(languageEnumeration)
                inline = false
            }
            field {
                name = "``/${container.styleCommand()}`` or ``~${container.styleCommand()}``"
                value = container.helpCommandEmbedStyle()
                inline = false
            }
            field {
                name = "``/${container.rankCommand()}`` or ``~${container.rankCommand()}``"
                value = container.helpCommandEmbedRank()
                inline = false
            }
            field {
                name = "``/${container.ratingCommand()}`` or ``~${container.ratingCommand()}``"
                value = container.helpCommandEmbedRating()
                inline = false
            }
        }

    override fun produceAboutBot(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(embeds = listOf(this.buildAboutEmbed(container), this.buildCommandGuideEmbed(container)))) }

    override fun paginateAboutBot(original: MessageAdaptor<Message, DiscordButtons>, container: LanguageContainer, page: Int) =
        IO { original.original.editMessage(
            when (page) {
                0 -> Message(embeds = listOf(this.buildAboutEmbed(container), this.buildCommandGuideEmbed(container)))
                else -> throw IllegalStateException()
            }
        ).queue() }

    override fun paginateSettings(original: MessageAdaptor<Message, DiscordButtons>, config: GuildConfig, page: Int) =
        IO {
            original.original
                .editMessageComponents()
                .flatMap { message ->
                    when (page) {
                        0 -> message.editMessage(Message(embed = this.languageEmbed))
                        1 -> message.editMessage(Message(embed = this.buildStyleGuideEmbed(config.language.container)))
                        2 -> message.editMessage(Message(embed = this.buildFocusPolicyGuideEmbed(config.language.container)))
                            .setActionRow(listOf(this.buildFocusPolicyMenu(config)))
                        3 -> message.editMessage(Message(embed = this.buildSweepPolicyGuideEmbed(config.language.container)))
                            .setActionRow(listOf(this.buildSweepPolicyMenu(config)))
                        4 -> message.editMessage(Message(embed = this.buildArchivePolicyGuideEmbed(config.language.container)))
                            .setActionRow(listOf(this.buildArchivePolicyMenu(config)))
                        else -> throw IllegalStateException()
                    }
                }.queue()
        }

    // RANK

    override fun produceRankings(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, rankings: Set<SimpleProfile>) =
        IO { publisher(Message(
            embed = Embed {
                title = container.rankEmbedTitle()
                description = container.rankEmbedDescription()
            }
        )) }

    // RATING

    override fun produceRating(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) = TODO()

    // LANG

    private val languageEmbed = Embed {
        color = COLOR_NORMAL_HEX
        title = "GomokuBot / Language"
        description = "The default language is set based on the server region. Please apply the proper language for this server."
        Language.values().fold(this) { builder, language ->
            builder.also { field {
                name = language.container.languageName()
                value = language.container.languageSuggestion()
                inline = false
            } }
        }
    }

    override fun produceLanguageGuide(publisher: DiscordMessagePublisher) =
        IO { publisher(Message(embed = languageEmbed)) }

    override fun produceLanguageNotFound(publisher: MessagePublisher<Message, DiscordButtons>) =
        IO { publisher(Message(content = "There is an error in the Language Code. Please select from the llist below.")) }

    override fun produceLanguageUpdated(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(content = container.languageUpdated())) }

    // SETTINGS

    private fun buildStyleGuideEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_NORMAL_HEX
            title = container.styleEmbedTitle()
            description = container.styleEmbedDescription()
            BoardStyle.values()
                .drop(1)
                .fold(this) { builder, style ->
                    builder.also {
                        field {
                            name = "Style ${style.sample.styleShortcut.asHighlightFormat()}:${style.sample.styleName.asHighlightFormat()}"
                            value = "${container.styleEmbedSuggestion(style.sample.styleShortcut.asHighlightFormat())}\n${style.sample.sampleView}"
                            inline = false
                        }
                    }
                }
            field {
                name = "Style ``A``:``IMAGE``"
                value = container.styleEmbedSuggestion("A".asHighlightFormat())
                inline = false
            }
            image = "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/style-preview.jpg"
        }

    private fun buildFocusPolicyGuideEmbed(container: LanguageContainer) =
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

    private fun buildFocusPolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.focusSelectIntelligence(),
                value = "f-i",
                emoji = EMOJI_ZAP,
                default = config.focusPolicy == FocusPolicy.INTELLIGENCE
            )
            option(
                label = config.language.container.focusSelectFallowing(),
                value = "f-f",
                emoji = EMOJI_MAG,
                default = config.focusPolicy == FocusPolicy.FALLOWING
            )
        }

    private fun buildSweepPolicyGuideEmbed(container: LanguageContainer) =
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

    private fun buildSweepPolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.sweepSelectRelay(),
                value = "s-r",
                emoji = EMOJI_BROOM,
                default = config.sweepPolicy == SweepPolicy.RELAY
            )
            option(
                label = config.language.container.sweepSelectLeave(),
                value = "s-l",
                emoji = EMOJI_CABINET,
                default = config.sweepPolicy == SweepPolicy.LEAVE
            )
        }

    private fun buildArchivePolicyGuideEmbed(container: LanguageContainer) =
        Embed {
            title = container.archiveEmbedTitle()
            description = container.archiveEmbedDescription()
            color = COLOR_NORMAL_HEX

            field {
                name = "$UNICODE_PERFORMING ${container.archiveSelectByAnonymous()}"
                value = container.archiveSelectByAnonymousDescription()
                inline = false
            }

            field {
                name = "$UNICODE_SILHOUETTE ${container.archiveSelectWithProfile()}"
                value = container.archiveSelectWithProfileDescription()
                inline = false
            }

            field {
                name = "$UNICODE_LOCK ${container.archiveSelectPrivacy()}"
                value = container.archiveSelectPrivacyDescription()
                inline = false
            }
        }

    private fun buildArchivePolicyMenu(config: GuildConfig) =
        SelectMenu("p") {
            option(
                label = config.language.container.archiveSelectByAnonymous(),
                value = "a-a",
                emoji = EMOJI_PERFORMING,
                default = config.archivePolicy == ArchivePolicy.BY_ANONYMOUS
            )
            option(
                label = config.language.container.archiveSelectWithProfile(),
                value = "a-p",
                emoji = EMOJI_SILHOUETTE,
                default = config.archivePolicy == ArchivePolicy.WITH_PROFILE
            )
            option(
                label = config.language.container.archiveSelectPrivacy(),
                value = "a-b",
                emoji = EMOJI_LOCK,
                default = config.archivePolicy == ArchivePolicy.PRIVACY
            )
        }

    // STYLE

    override fun produceStyleGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(embed = this.buildStyleGuideEmbed(container))) }

    override fun produceStyleNotFound(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.styleErrorNotfound(user.asMentionFormat()))) }

    override fun produceStyleUpdated(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, style: String) =
        IO { publisher(Message(content = container.styleUpdated(style))) }

    // POLICY

    override fun produceConfigApplied(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, configKind: String, configChoice: String) =
        IO { publisher(Message(content = container.configApplied(configChoice.asHighlightFormat()))) }

    // SESSION

    override fun produceSessionNotFound(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.sessionNotFound(user.asMentionFormat()))) }

    // START

    override fun produceSessionAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(content = container.startErrorSessionAlready(owner.asMentionFormat()))) }

    override fun produceOpponentSessionAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorOpponentSessionAlready(owner.asMentionFormat(), opponent.asMentionFormat())))}

    override fun produceRequestAlreadySent(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorRequestAlreadySent(owner.asMentionFormat(), opponent.asMentionFormat())))}

    override fun produceRequestAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorRequestAlready(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceOpponentRequestAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.startErrorOpponentRequestAlready(owner.asMentionFormat(), opponent.asMentionFormat())))}

    // SET

    override fun produceOrderFailure(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, player: User) =
        IO { publisher(Message(content = container.processErrorOrder(user.asMentionFormat(), player.asMentionFormat()))) }

    override fun produceSetIllegalArgument(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.setErrorIllegalArgument(user.asMentionFormat()))) }

    override fun produceSetAlreadyExist(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, pos: Pos) =
        IO { publisher(Message(content = container.setErrorExist(user.asMentionFormat(), pos.toCartesian().asHighlightFormat()))) }

    override fun produceSetForbiddenMove(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte) =
        IO { publisher(Message(content = container.setErrorForbidden(user.asMentionFormat(), pos.toCartesian().asHighlightFormat(), forbiddenFlagToText(forbiddenFlag).asHighlightFormat()))) }

    // REQUEST

    override fun produceRequest(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
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

    override fun produceRequestRejected(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.requestRejected(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceRequestExpired(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(content = container.requestExpired(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    // UTILS

    override fun produceNotYetImplemented(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, officialChannel: String) =
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
