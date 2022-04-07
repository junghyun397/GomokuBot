package discord.interact.message

import core.assets.*
import core.database.entities.SimpleProfile
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.*
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.BoardStyle
import core.session.ArchivePolicy
import core.session.entities.GameSession
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.InlineEmbed
import dev.minn.jda.ktx.Message
import discord.assets.EMOJI_BLACK_CIRCLE
import discord.assets.EMOJI_DARK_X
import discord.assets.EMOJI_WHITE_CIRCLE
import jrenju.notation.Pos
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.RestAction
import utils.structs.IO

object DiscordMessageProducer : MessageProducer<Message, DiscordButtons>() {

    // BOARD

    private fun InlineEmbed.buildBoardAuthor(container: LanguageContainer, session: GameSession) =
        this.author {
            iconUrl = session.owner.profileURL
            name = StringBuilder().apply {
                append(session.owner.name)
                append(if (session.ownerHasBlack) UNICODE_BLACK_CIRCLE else UNICODE_WHITE_CIRCLE)
                append(" vs ")
                append(session.opponent.fold(
                    onDefined = { it.name },
                    onEmpty = { "AI" }
                ))
                append(if (session.ownerHasBlack) UNICODE_WHITE_CIRCLE else UNICODE_BLACK_CIRCLE)
                append(", ")
                append(if (session.board.isEnd) container.boardFinished() else container.boardInProgress())
            }.toString()
        }

    private fun InlineEmbed.buildBoardFields(container: LanguageContainer, session: GameSession) =
        this.apply {
            field {
                name = container.boardMoves()
                value = "``${session.board.moves()}``"
                inline = true
            }
            field {
                name = container.boardLatestMove()
                value = "``${session.board.latestPos().toCartesian()}``"
                inline = true
            }
        }

    private fun buildNextMoveEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_GREEN_HEX
            description = container.boardCommandGuide()
        }

    override fun produceBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageAction<DiscordButtons>> {
        val barColor = if (session.board.isEnd) COLOR_RED_HEX else COLOR_GREEN_HEX
        return renderer.renderBoard(session.board).fold(
            onLeft = { textBoard -> IO {
                publisher(Message(
                    embeds = listOf(
                        Embed {
                            color = barColor

                            this.buildBoardAuthor(container, session)
                            description = textBoard
                            this.buildBoardFields(container, session)
                        },
                        this.buildNextMoveEmbed(container)
                    )
                ))
            } },
            onRight = { imageBoard -> IO {
                publisher(Message(
                    embeds = listOf(
                        Embed {
                            color = barColor

                            this.buildBoardAuthor(container, session)
                            this.buildBoardFields(container, session)
                            image = "attachment://${imageBoard.second}"
                        },
                        this.buildNextMoveEmbed(container)
                    )
                )).addFile(imageBoard.first, imageBoard.second)
            } }
        )
    }

    override fun attachButtons(boardAction: MessageAction<DiscordButtons>, container: LanguageContainer, focused: FocusedFields) =
        boardAction.addButtons(
            focused.map { row -> ActionRow.of(
                row.map { element -> when (element.second) {
                    ButtonFlag.FREE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", element.first)
                    ButtonFlag.BLACK -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                    ButtonFlag.WHITE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                    ButtonFlag.BLACK_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                    ButtonFlag.WHITE_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                    ButtonFlag.FORBIDDEN -> Button.of(ButtonStyle.DANGER, "s-${element.first}", "", EMOJI_DARK_X).asDisabled()
                } }
            ) }.reversed() // cartesian coordinate system
        )

    fun sendBoardArchive(session: GameSession, archivePolicy: ArchivePolicy, publisher: (Message) -> RestAction<Message>) =
        publisher(Message(
            embed = Embed {
            }
        ))

    // GAME

    override fun produceBeginsPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) = TODO()

    override fun produceBeginsPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) = TODO()

    override fun produceNextMove(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos) = TODO()

    override fun produceWinPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos) = TODO()

    override fun produceTiePVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) = TODO()

    override fun produceWinPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) = TODO()

    override fun produceLosePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) = TODO()

    override fun produceTiePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) = TODO()

    override fun produceSurrendered(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User) = TODO()

    // HELP

    override fun produceAboutBot(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.helpAboutEmbedTitle()
                description = container.helpAboutEmbedDescription()
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
                    name = "$UNICODE_ZAP Powered by Kotlin, Project Reactor, R2DBC, gRPC, JDA"
                }
            }
        )) }

    override fun produceCommandGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.helpCommandEmbedTitle()
                description = container.helpCommandDescription()
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
        )) }

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
        IO { publisher(Message(content = "Language Not Found")) }

    override fun produceLanguageUpdated(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(content = container.languageUpdated())) }

    // STYLE

    override fun produceStyleGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.styleEmbedTitle()
                description = container.styleEmbedDescription()
                BoardStyle.values()
                    .drop(1)
                    .fold(this) { builder, style ->
                        builder.also {
                            field {
                                name = "Style ``${style.sample.styleShortcut}``:``${style.sample.styleName}``"
                                value = style.sample.sampleView
                                inline = false
                            }
                        }
                    }
                field {
                    name = "Style ``A``:``IMAGE``"
                    inline = false
                }
                image = "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/style-preview.jpg"
            }
        )) }

    override fun produceStyleNotFound(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(content = container.styleErrorNotfound())) }

    override fun produceStyleUpdated(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, style: String) =
        IO { publisher(Message(content = container.styleUpdated(style))) }

    // POLICY

    // SESSION

    override fun produceSessionNotFound(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(content = container.sessionNotFound())) }

    // START

    override fun produceSessionAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, opponent: User) =
        IO { publisher(Message(content = container.startErrorSessionAlready(opponent.nameTag))) }

    override fun produceRequestAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(content = container.startErrorRequestAlready(owner.nameTag))) }

    // SET

    override fun produceSetIllegalArgument(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(content = container.setErrorIllegalArgument())) }

    override fun produceSetAlreadyExist(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, pos: Pos) =
        IO { publisher(Message(content = container.setErrorExist(user.nameTag, pos.toCartesian()))) }

    override fun produceSetForbiddenMove(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte) =
        IO { publisher(Message(content = container.setErrorForbidden(user.nameTag, pos.toCartesian(), forbiddenFlagToText(forbiddenFlag)))) }

    // REQUEST

    override fun produceRequest(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_GREEN_HEX
                title = container.requestEmbedTitle()
                description = container.requestEmbedDescription(owner.nameTag, opponent.nameTag)
            }
        )).addButtons(
            listOf(ActionRow.of(
                Button.of(ButtonStyle.DANGER, "r-${owner.id.idLong}", container.requestEmbedButtonReject()),
                Button.of(ButtonStyle.SUCCESS, "a-${owner.id.idLong}", container.requestEmbedButtonAccept())
            ))
        ) }

    // UTILS

    override fun produceNotYetImplemented(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
                description = container.notYetImplementedEmbedDescription()
                footer {
                    name = "$UNICODE_MAILBOX ${container.notYetImplementedEmbedFooter()}"
                }
            }
        )) }

    fun sendPermissionNotGrantedEmbed(container: LanguageContainer, channelName: String, publisher: (Message) -> RestAction<Message>) =
        publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle()}"
                description = container.permissionNotGrantedEmbedDescription(channelName)
                footer {
                    name = "$UNICODE_ALARM_CLOCK ${container.permissionNotGrantedEmbedFooter()}"
                }
            }
        ))

}
