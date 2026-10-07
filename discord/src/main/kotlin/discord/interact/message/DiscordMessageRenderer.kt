package discord.interact.message

import core.assets.*
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.*
import core.interact.message.graphics.HistoryRenderType
import core.interact.message.graphics.ImageBoardRenderer
import core.session.entities.*
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import discord.assets.EMOJI_DARK_X
import discord.assets.EMOJI_STONE
import discord.interact.message.DiscordComponentIds.ACCEPT
import discord.interact.message.DiscordComponentIds.OPENING
import discord.interact.message.DiscordComponentIds.REJECT
import discord.interact.message.DiscordComponentIds.REPLAY
import discord.interact.message.DiscordComponentIds.SET
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.selections.SelectMenu
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.FileUpload
import renju.notation.Color
import renju.notation.GameResult
import renju.notation.Pos
import renju.notation.map
import utils.memoize
import utils.tuple
import utils.unreachable
import kotlin.reflect.KClass

object DiscordMessageRenderer {

    const val FOCUS_WIDTH = 5

    fun render(message: AppMessage): DiscordMessageData = when (message) {
        is AppMessage.Text -> DiscordMessageData(content = message.content)
        is AppMessage.Embed -> DiscordMessageData(embed = this.renderEmbed(message))
        is AppMessage.LanguageGuide -> DiscordMessageData(embed = this.renderLanguageGuide())
        is AppMessage.Announcement -> DiscordMessageData(embed = this.renderAnnouncement(message))
        is AppMessage.Rankings -> DiscordMessageData(embed = this.renderRankings(message))
        is AppMessage.Rating -> DiscordMessageData(embed = this.renderRating(message))
        is AppMessage.GameStarted -> DiscordMessageData(embed = this.renderGameStarted(message))
        is AppMessage.GameFinished -> DiscordMessageData(embed = this.renderGameFinished(message))
        is AppMessage.Board -> this.renderBoard(message.view)
        is AppMessage.BoardArchive -> this.renderArchive(message.draw)
        is AppMessage.Replay -> this.renderArchive(message.draw)
        is AppMessage.ReplayList -> this.renderReplayList(message.view)
        is AppMessage.Help -> this.renderHelp(message.container, message.page)
        is AppMessage.Settings -> this.renderSettings(message.config, message.page)
        is AppMessage.Request -> DiscordMessageData(
            embeds = listOf(Embed {
                this.color = COLOR_GREEN_HEX
                this.title = requestTitle(message.container, message.session)
                this.description = requestDescription(message.container, message.session, message.session.requester.asMention(), message.session.recipient.asMention())
            }),
            components = listOf(ActionRow.of(
                Button.of(ButtonStyle.DANGER, "$REJECT-${message.session.id.uuid}", message.container.requestEmbedButtonReject),
                Button.of(ButtonStyle.SUCCESS, "$ACCEPT-${message.session.id.uuid}", message.container.requestEmbedButtonAccept),
            )),
        )
    }

    private fun renderEmbed(message: AppMessage.Embed): MessageEmbed = Embed {
        this.title = message.title
        this.description = message.description
        this.color = when (message.level) {
            NoticeLevel.INFO -> COLOR_NORMAL_HEX
            NoticeLevel.ERROR -> COLOR_RED_HEX
        }
    }

    private fun renderLanguageGuide(color: Int = COLOR_NORMAL_HEX): MessageEmbed = Embed {
        this.color = color
        this.title = "GomokuBot / Language"
        this.description = "Default language is set based on the server region. Please apply proper language for this server."
        Language.entries.forEach { language ->
            this.field {
                this.name = "${language.container.languageName} (`${language.container.languageCode}`)"
                this.value = language.container.languageSuggestion
                this.inline = false
            }
        }
    }

    private fun renderAnnouncement(message: AppMessage.Announcement): MessageEmbed = Embed {
        this.color = COLOR_NORMAL_HEX
        this.title = message.title
        this.description = message.content
        this.footer { this.name = message.publishedOn }
    }

    private fun renderRankings(message: AppMessage.Rankings): MessageEmbed = Embed {
        val container = message.container
        val win = container.rankEmbedWin
        val lose = container.rankEmbedLose
        val draw = container.rankEmbedDraw
        this.color = COLOR_NORMAL_HEX
        this.title = container.rankEmbedTitle
        this.description = container.rankEmbedDescription
        message.entries.forEachIndexed { index, (profile, stats) ->
            this.field {
                this.name = "#${index + 1} ${profile.name}"
                this.value = """
                    **$UNICODE_TROPHY$win: `${stats.totalWins}`, $UNICODE_WHITE_FLAG️$lose: `${stats.totalLosses}`, $UNICODE_PENCIL️$draw: `${stats.totalDraws}`**
                    `${UNICODE_STONE[Color.BLACK]}`$win: `${stats.blackWins}`, `${UNICODE_STONE[Color.BLACK]}`$lose: `${stats.blackLosses}`,`${UNICODE_STONE[Color.BLACK]}`$draw: `${stats.blackDraws}`
                    `${UNICODE_STONE[Color.WHITE]}`$win: `${stats.whiteWins}`, `${UNICODE_STONE[Color.WHITE]}`$lose: `${stats.whiteLosses}`,`${UNICODE_STONE[Color.WHITE]}`$draw: `${stats.whiteDraws}`
                """.trimIndent()
                this.inline = false
            }
        }
    }

    private fun renderRating(message: AppMessage.Rating): MessageEmbed = Embed {
        this.color = COLOR_NORMAL_HEX
        this.title = message.rating.toString()
        this.author {
            this.name = message.user.name
            this.iconUrl = message.user.profileURL
        }
        this.field {
            this.name = "Recent Change"
            this.value = message.recentDelta.toString()
        }
    }

    private fun renderGameStarted(message: AppMessage.GameStarted): MessageEmbed = Embed {
        this.color = COLOR_GREEN_HEX
        this.description = message.description
        this.buildGameAuthor(message)
        message.enginePlayer?.let { enginePlayer ->
            val players = message.users.map { user ->
                when (user) {
                    is User.Human -> user.asMention()
                    is User.GomokuBot -> enginePlayer
                }
            }

            this.field {
                this.name = message.container.gameStartBlack
                this.value = players.black
                this.inline = true
            }
            this.field {
                this.name = message.container.gameStartWhite
                this.value = players.white
                this.inline = true
            }
        }
        this.field {
            this.name = message.container.gameStartRule
            this.value = message.rule.display
            this.inline = true
        }
    }

    private fun renderGameFinished(message: AppMessage.GameFinished): MessageEmbed = Embed {
        this.color = COLOR_NORMAL_HEX
        this.description = message.description
        this.buildBoardAuthor(message.container, message.draw)
        message.draw.eloRating?.let { (rating, delta) ->
            this.field {
                this.name = message.container.gameResultEngineRating
                this.value = rating.toString()
                this.inline = true
            }
            this.field {
                this.name = message.container.gameResultEngineRatingChange
                this.value = delta.toString()
                this.inline = true
            }
        }
    }

    private fun User.withColor(color: Color) = "${this.name}${UNICODE_STONE[color]}"

    private fun User.Human.asMention() = if (this.isAnonymous) this.name else "<@${this.givenId.idLong}>"

    private fun String.asHighlightFormat() = "`$this`"

    private fun String.asBoldFormat() = "**$this**"

    private fun ActionRowChildComponent.liftToButtons() = listOf(ActionRow.of(this))

    private fun InlineEmbed.buildBoardAuthor(container: LanguageContainer, draw: GameDraw) =
        this.buildGameAuthor(draw, if (draw.result == null) container.boardInProgress else container.boardFinished)

    private fun InlineEmbed.buildGameAuthor(participants: GameParticipants, status: String? = null) =
        this.author {
            this.iconUrl = participants.users[participants.leaderColor].profileURL
            this.name = buildString {
                append(participants.users[participants.leaderColor].withColor(participants.leaderColor))
                append(" vs ")
                append(participants.users[!participants.leaderColor].withColor(!participants.leaderColor))

                status?.let {
                    append(", ")
                    append(it)
                }
            }
        }

    private fun InlineEmbed.buildStatusFields(container: LanguageContainer, draw: BoardDraw) {
        draw.state.history.lastOrNull()
            ?.let { lastPos ->
                this.field {
                    this.name = container.boardMoves
                    this.value = draw.state.history.size.toString().asHighlightFormat()
                    this.inline = true
                }

                this.field {
                    this.name = container.boardLastMove
                    this.value = "${UNICODE_STONE[!draw.state.board.playerColor]}${lastPos}".asHighlightFormat()
                    this.inline = true
                }
            }
    }

    private fun InlineEmbed.buildResultFields(container: LanguageContainer, draw: BoardDraw, gameResult: GameResult) {
        this.field {
            this.name = container.boardMoves
            this.value = draw.state.history.size.toString().asHighlightFormat()
            this.inline = true
        }

        this.field {
            this.name = container.boardResult
            this.value = when (gameResult) {
                is GameResult.Win -> {
                    val winner = draw.users[gameResult.winner]

                    container.boardWinDescription(
                        "${winner.name}${UNICODE_STONE[gameResult.winner]}".asHighlightFormat()
                    )
                }
                is GameResult.Full -> { container.boardTieDescription.asHighlightFormat() }
            }
            this.inline = true
        }
    }

    private fun renderBoard(view: BoardView): DiscordMessageData {
        val renderType = if (view.draw.result != null) HistoryRenderType.SEQUENCE else view.renderType
        val board = view.style.renderer.renderBoard(view.draw.state, renderType, view.offers, view.blinds)
        val fileName = ImageBoardRenderer.newFileName()
        val files = board.fold(
            ifLeft = { emptyList() },
            ifRight = { listOf(FileUpload.fromData(it, fileName)) },
        )
        val embeds = buildList {
            add(Embed {
                this.color = if (view.draw.result != null) COLOR_RED_HEX else COLOR_GREEN_HEX
                this.buildBoardAuthor(view.container, view.draw)
                view.draw.result?.let { this.buildResultFields(view.container, view.draw, it) }
                    ?: this.buildStatusFields(view.container, view.draw)
                board.fold(
                    ifLeft = { this.description = it },
                    ifRight = { this.image = "attachment://$fileName" },
                )
            })
            this@DiscordMessageRenderer.renderBoardGuide(view)?.let { add(it) }
        }
        return DiscordMessageData(embeds = embeds, files = files, components = this.renderBoardControls(view))
    }

    private fun renderBoardGuide(view: BoardView): MessageEmbed? {
        val container = view.container
        val text = when (val interaction = view.interaction) {
            is BoardInteraction.Place -> {
                val remainingUndos = interaction.remainingUndos

                when {
                    view.draw.state.history.size < 3 || view.openingPlayer != null -> container.boardCommandGuide
                    remainingUndos == null -> container.boardUndoGuide
                    remainingUndos == 0 -> container.boardResignGuide
                    remainingUndos == 5 -> container.boardUndoInitialGuide
                    else -> container.boardUndoRemainingGuide(remainingUndos)
                }
            }
            is BoardInteraction.Offer -> container.boardOfferGuide(interaction.remaining)
            is BoardInteraction.Select -> container.boardSelectGuide
            is BoardInteraction.Swap -> interaction.offerCount?.let { container.boardStatefulSwapGuide(it) } ?: container.boardSwapGuide
            BoardInteraction.Branch -> container.boardBranchGuide
            is BoardInteraction.Declare -> container.boardDeclareGuide
            BoardInteraction.Finished -> return null
        }

        return Embed {
            this.color = COLOR_GREEN_HEX
            this.description = text
            view.openingPlayer?.let { player ->
                this.footer {
                    this.iconUrl = player.profileURL
                    this.name = "${player.name}'s turn."
                }
            }
        }
    }

    private fun renderBoardControls(view: BoardView): List<MessageTopLevelComponent> = when (val interaction = view.interaction) {
        is BoardInteraction.Place -> this.renderInputField(interaction.input)
        is BoardInteraction.Offer -> this.renderInputField(interaction.input)
        is BoardInteraction.Select -> this.renderInputField(interaction.input)
        is BoardInteraction.Swap -> listOf(ActionRow.of(
            Button.of(ButtonStyle.PRIMARY, "$OPENING-sy", view.container.swapSelectYes),
            Button.of(ButtonStyle.SECONDARY, "$OPENING-sn", view.container.swapSelectNo),
        ))
        BoardInteraction.Branch -> listOf(ActionRow.of(
            Button.of(ButtonStyle.PRIMARY, "$OPENING-bn", view.container.branchSelectSwap),
            Button.of(ButtonStyle.SECONDARY, "$OPENING-by", view.container.branchSelectOffer),
        ))
        is BoardInteraction.Declare -> StringSelectMenu(OPENING.toString()) {
            for (count in 1..interaction.maxCount) {
                option(label = count.toString(), value = "$OPENING-d$count", default = false)
            }
        }.liftToButtons()
        BoardInteraction.Finished -> emptyList()
    }

    private fun renderArchive(draw: BoardDraw): DiscordMessageData {
        val fileName = ImageBoardRenderer.newFileName()
        val image = ImageBoardRenderer.renderInputStream(draw.state, HistoryRenderType.SEQUENCE, null, null, true)
        return DiscordMessageData(
            embeds = listOf(Embed {
                this.color = COLOR_NORMAL_HEX
                this.buildBoardAuthor(Language.ENG.container, draw)
                draw.result?.let { this.buildResultFields(Language.ENG.container, draw, it) }
                    ?: this.buildStatusFields(Language.ENG.container, draw)
                this.image = "attachment://$fileName"
            }),
            files = listOf(FileUpload.fromData(image, fileName)),
        )
    }

    fun renderInputField(input: BoardInput): List<ActionRow> {
        val half = this.FOCUS_WIDTH / 2
        return (-half..half).map { rowOffset ->
            ActionRow.of((-half..half).map { colOffset ->
                val pos = Pos(input.focus.focus.row + rowOffset, input.focus.focus.col + colOffset)
                this.renderInputButton(input, pos)
            })
        }.reversed()
    }

    private fun renderInputButton(input: BoardInput, pos: Pos): Button {
        val id = "$SET-$pos"
        val stone = input.state.board.stoneKind(pos)
        if (stone != null) {
            val style = if (pos == input.state.history.lastOrNull()) ButtonStyle.SUCCESS else ButtonStyle.SECONDARY
            return Button.of(style, id, "", EMOJI_STONE[stone]).asDisabled()
        }
        return when {
            input.state.board.playerColor == Color.BLACK && input.state.board.forbiddenKind(pos) != null ->
                Button.of(ButtonStyle.DANGER, id, "", EMOJI_DARK_X).asDisabled()
            input.focus.hints?.contains(pos) == true -> Button.of(ButtonStyle.PRIMARY, id, pos.toString())
            pos !in input.legalMoves -> Button.of(ButtonStyle.SECONDARY, id, pos.toString()).asDisabled()
            pos in input.choices -> Button.of(ButtonStyle.PRIMARY, id, pos.toString())
            else -> Button.of(ButtonStyle.SECONDARY, id, pos.toString())
        }
    }

    private fun renderReplayList(view: ReplayListView): DiscordMessageData {
        val container = view.container
        val player = view.player
        val embedBuilder = EmbedBuilder(color = COLOR_NORMAL_HEX)

        val selectMenuOptions = mutableListOf<SelectOption>()

        view.entries.forEachIndexed { idx, record ->
            val userColor = record.playerColor
            val opponent = record.opponent

            val result = when (record.winner) {
                null -> "$UNICODE_PENCIL${container.replayEmbedDraw}"
                userColor -> "$UNICODE_TROPHY${container.replayEmbedWin}"
                else -> "$UNICODE_WHITE_FLAG${container.replayEmbedLose}"
            }

            embedBuilder.field {
                this.name = "#${idx + 1}: `${player.withColor(userColor)}` vs `${opponent.withColor(!userColor)}`, `$result`"
                this.value = "`${record.date}`, `${record.rule}`, `${container.replayEmbedMatchInfo(record.moves)}`"
                this.inline = false
            }

            selectMenuOptions.add(
                SelectOption.of(
                    "#${idx + 1}: ${player.withColor(userColor)} vs ${opponent.withColor(!userColor)}, $result",
                    "$REPLAY-${record.id.id}-1-${player.id.validationKey}"
                )
            )
        }

        return DiscordMessageData(
            embeds = listOf(embedBuilder.build()),
            components = StringSelectMenu(REPLAY.toString(), options = selectMenuOptions).liftToButtons(),
        )
    }

    private val aboutEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            this.color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            this.title = container.helpAboutEmbedTitle
            this.description = container.helpAboutEmbedDescription("Discord")
            this.thumbnail =
                "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/profile-thumbnail.jpg"

            this.field {
                this.name = container.helpAboutEmbedDeveloper
                this.value = "@do1phin"
            }
            this.field {
                this.name = container.helpAboutEmbedRepository
                this.value = "[github/GomokuBot](https://github.com/junghyun397/GomokuBot)"
            }
            this.field {
                this.name = container.helpAboutEmbedVersion
                this.value = "4.0"
            }
            this.field {
                this.name = container.helpAboutEmbedSupport
                this.value = "[discord/vq8pkfF](https://discord.gg/vq8pkfF)"
            }
            this.field {
                this.name = container.helpAboutEmbedInvite
                this.value = "[discord/oauth2](https://discord.com/api/oauth2/authorize?client_id=452520939792498689&permissions=137439266880&scope=bot%20applications.commands)"
            }

            this.footer {
                this.name = "$UNICODE_ZAP Powered by Kotlin, Rust, JDA and mintaka. Since 2018 GomokuBot."
            }
        }
    }

    private val commandGuideEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            this.color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            this.title = container.commandUsageEmbedTitle

            buildableCommands
                .flatMap { it.getLocalizedUsages(container) }
                .forEach { (usage, description) ->
                    this.field {
                        this.name = usage
                        this.value = description
                        this.inline = false
                    }
                }
        }
    }

    private val exploreAboutRenjuEmbed: (LanguageContainer) -> MessageEmbed = memoize { container ->
        Embed {
            this.color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, 0)
            this.description = container.exploreAboutRenju
        }
    }

    private val buildAboutRenjuEmbed: (Pair<Int, LanguageContainer>) -> List<MessageEmbed> = memoize { (page, container) ->
        val (h2Title, h2Documents) = HelpPages.documents[container]!!.first[page]

        h2Documents
            .flatMapIndexed { h2Index, (h3Title, blocks) -> blocks
                .mapIndexed { blockIndex, block ->
                    Embed {
                        this.color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.ABOUT, page + 1)
                        this.title = when {
                            h2Index == 0 && blockIndex == 0 -> h2Title.asBoldFormat()
                            blockIndex == 0 -> h3Title
                            else -> null
                        }

                        block.fold(
                            ifLeft = { this.image = it },
                            ifRight = { this.description = it }
                        )
                    }
                }
            }
    }

    private fun renderHelp(container: LanguageContainer, page: Int): DiscordMessageData =
        DiscordMessageData(embeds = when (page) {
            0 -> listOf(this.aboutEmbed(container), this.commandGuideEmbed(container), this.exploreAboutRenjuEmbed(container))
            else -> this.buildAboutRenjuEmbed(tuple(page - 1, container))
        })

    private fun renderSettings(config: ChannelConfig, page: Int): DiscordMessageData {
        if (page == 0) {
            val color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.SETTINGS, 0)
            return DiscordMessageData(embed = this.renderLanguageGuide(color))
        }
        val kind = when (page) {
            1 -> BoardStyle::class
            2 -> FocusType::class
            3 -> HintType::class
            4 -> HistoryRenderType::class
            5 -> ArchivePolicy::class
            else -> unreachable()
        }
        return DiscordMessageData(
            embeds = listOf(this.settingEmbed(kind)(config.language.container)),
            components = this.settingMenu(kind)(config.language.container)(config).liftToButtons(),
        )
    }

    private val settingEmbed: (KClass<*>) -> (LanguageContainer) -> MessageEmbed =
        memoize { classTag -> memoize { container ->
            val (settingElement, optionElements) = SettingMapping.map[classTag]!!

            Embed {
                this.color = PageNavigationState.encodeToColor(COLOR_NORMAL_HEX, NavigationKind.SETTINGS, settingElement.menuIndex)
                this.title = "GomokuBot / ${settingElement.label(container)}"
                this.description = settingElement.description(container)

                optionElements.forEach { (_, optionElement) ->
                    this.field {
                        this.name = "${optionElement.emoji} ${optionElement.label(container)}"
                        this.value = optionElement.description(container)
                        this.inline = false
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

}
