package discord.interact.message

import core.assets.*
import core.database.entities.SimpleProfile
import core.inference.InferenceManager
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.ButtonFlag
import core.interact.message.MessageAction
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.message.graphics.BoardRenderer
import core.interact.message.graphics.BoardStyle
import core.interact.message.graphics.ImageBoardRenderer
import core.session.FocusPolicy
import core.session.GameResult
import core.session.entities.GameSession
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.InlineEmbed
import dev.minn.jda.ktx.Message
import discord.assets.EMOJI_BLACK_CIRCLE
import discord.assets.EMOJI_DARK_X
import discord.assets.EMOJI_WHITE_CIRCLE
import jrenju.notation.Pos
import jrenju.notation.Renju
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.RestAction
import utils.structs.IO

object DiscordMessageProducer : MessageProducer<Message, DiscordButtons>() {

    private fun User.asMentionFormat() = "<@${this.id.idLong}>"

    // BOARD

    private fun InlineEmbed.buildBoardAuthor(container: LanguageContainer, session: GameSession) =
        this.author {
            iconUrl = session.owner.profileURL
            name = StringBuilder().apply {
                append(session.ownerWithColor())
                append(" vs ")
                append(session.opponentWithColor())
                append(", ")
                append(if (session.gameResult.isDefined) container.boardFinished() else container.boardInProgress())
            }.toString()
        }

    private fun InlineEmbed.buildMovesFields(container: LanguageContainer, session: GameSession) =
        this.apply {
            session.board.latestPos().foreach {
                field {
                    name = container.boardMoves()
                    value = "``${session.board.moves()}``"
                    inline = true
                }
                field {
                    name = container.boardLatestMove()
                    value = "``${session.board.latestPos().get().toCartesian()}``"
                    inline = true
                }
            }
        }

    private fun InlineEmbed.buildResultFields(container: LanguageContainer, session: GameSession, gameResult: GameResult) =
        this.apply {
            field {
                name = container.boardMoves()
                value = "``${session.board.moves()}``"
                inline = true
            }
            field {
                name = container.boardResult()
                value = when (gameResult) {
                    is GameResult.Win -> { container.boardWinDescription(gameResult.winner.withColor(gameResult.winColor)) }
                    is GameResult.Full -> { container.boardTieDescription() }
                }
                inline = true
            }
        }

    private fun buildNextMoveEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_GREEN_HEX
            description = container.boardCommandGuide()
        }

    override fun produceBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageAction<DiscordButtons>> {
        val barColor = session.gameResult.fold(onDefined = { COLOR_RED_HEX }, onEmpty = { COLOR_GREEN_HEX })
        return renderer.renderBoard(session.board).fold(
            onLeft = { textBoard -> IO {
                publisher(Message(
                    embeds = mutableListOf<MessageEmbed>().apply {
                        add(Embed {
                            color = barColor

                            this.buildBoardAuthor(container, session)
                            description = textBoard

                            session.gameResult.fold(
                                onDefined = { this.buildResultFields(container, session, it) },
                                onEmpty = { this.buildMovesFields(container, session) },
                            )
                        })
                        if (session.gameResult.isEmpty) add(this@DiscordMessageProducer.buildNextMoveEmbed(container))
                    }
                ))
            } },
            onRight = { imageBoard -> IO {
                publisher(Message(
                    embeds = mutableListOf<MessageEmbed>().apply {
                        add(Embed {
                            color = barColor

                            this.buildBoardAuthor(container, session)

                            session.gameResult.fold(
                                onDefined = { this.buildResultFields(container, session, it) },
                                onEmpty = { this.buildMovesFields(container, session) },
                            )

                            image = "attachment://${imageBoard.second}"
                        })
                        if (session.gameResult.isEmpty) add(this@DiscordMessageProducer.buildNextMoveEmbed(container))
                    }
                )).addFile(imageBoard.first, imageBoard.second)
            } }
        )
    }

    override fun producePublicBoard(publisher: MessagePublisher<Message, DiscordButtons>, session: GameSession): IO<MessageAction<DiscordButtons>> {
        val board = ImageBoardRenderer.renderBoard(session.board, true)
        return IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX

                this.buildBoardAuthor(Language.ENG.container, session)
                this.buildMovesFields(Language.ENG.container, session)

                field {
                    name = "winner"
                    inline = true
                }
                field {
                    name = ""
                    inline = true
                }
                image = "attachment://${board.second}"
            }
        )).addFile(board.first, board.second) }
    }

    override fun attachFocusButtons(boardAction: MessageAction<DiscordButtons>, container: LanguageContainer, focusPolicy: FocusPolicy, session: GameSession): MessageAction<DiscordButtons> {
        val focus = when (focusPolicy ) {
            FocusPolicy.INTELLIGENCE -> InferenceManager.resolveFocus(session.board, 5)
            FocusPolicy.FALLOWING -> session.board.latestPos().fold(
                { Renju.BOARD_CENTER() },
                { Pos(it.row().coerceIn(2, Renju.BOARD_MAX_IDX() - 2), it.col().coerceIn(2, Renju.BOARD_MAX_IDX() - 2)) }
            )
        }

        return boardAction.addButtons(
            this.generateFocusedField(session.board, focus, 5).map { col -> ActionRow.of(
                col.map { element -> when (element.second) {
                    ButtonFlag.FREE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", element.first)
                    ButtonFlag.BLACK -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                    ButtonFlag.WHITE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                    ButtonFlag.BLACK_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                    ButtonFlag.WHITE_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                    ButtonFlag.FORBIDDEN -> Button.of(ButtonStyle.DANGER, "s-${element.first}", "", EMOJI_DARK_X).asDisabled()
                    ButtonFlag.HIGHLIGHTED -> Button.of(ButtonStyle.PRIMARY, "s-${element.first}", element.first)
                } }
            ) }.reversed()
        )
    }

    // GAME

    override fun produceBeginsPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(container.beginPVP(owner.asMentionFormat(), opponent.asMentionFormat(), owner.asMentionFormat()))) }

    override fun produceBeginsPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.beginPVE(owner.asMentionFormat(), owner.asMentionFormat())))}

    override fun produceNextMove(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos) =
        IO { publisher(Message(container.processNext(nextPlayer.asMentionFormat(), previousPlayer.asMentionFormat(), latestMove.toCartesian()))) }

    override fun produceWinPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVPWin(winner.asMentionFormat(), looser.asMentionFormat(), latestMove.toCartesian())))}

    override fun produceTiePVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, opponent: User) =
        IO { publisher(Message(container.endPVPTie(owner.asMentionFormat(), opponent.asMentionFormat()))) }

    override fun produceSurrenderedPVP(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, winner: User, looser: User) =
        IO { publisher(Message(container.endPVPResign(winner.asMentionFormat(), looser.asMentionFormat())))}

    override fun produceWinPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVEWin(owner.asMentionFormat(), latestMove.toCartesian()))) }

    override fun produceLosePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User, latestMove: Pos) =
        IO { publisher(Message(container.endPVELose(owner.asMentionFormat(), latestMove.toCartesian())))}

    override fun produceTiePVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.endPVETie(owner.asMentionFormat()))) }

    override fun produceSurrenderedPVE(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(container.endPVEResign(owner.asMentionFormat())))}

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
                                value = "${container.styleEmbedSuggestion(style.sample.styleShortcut)}\n${style.sample.sampleView}"
                                inline = false
                            }
                        }
                    }
                field {
                    name = "Style ``A``:``IMAGE``"
                    value = container.styleEmbedSuggestion("A")
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
        IO { publisher(Message(content = container.startErrorSessionAlready(opponent.asMentionFormat()))) }

    override fun produceRequestAlready(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, owner: User) =
        IO { publisher(Message(content = container.startErrorRequestAlready(owner.asMentionFormat()))) }

    // SET

    override fun produceSetIllegalArgument(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User) =
        IO { publisher(Message(content = container.setErrorIllegalArgument(user.asMentionFormat()))) }

    override fun produceSetAlreadyExist(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, pos: Pos) =
        IO { publisher(Message(content = container.setErrorExist(user.asMentionFormat(), pos.toCartesian()))) }

    override fun produceSetForbiddenMove(publisher: MessagePublisher<Message, DiscordButtons>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte) =
        IO { publisher(Message(content = container.setErrorForbidden(user.asMentionFormat(), pos.toCartesian(), forbiddenFlagToText(forbiddenFlag)))) }

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
