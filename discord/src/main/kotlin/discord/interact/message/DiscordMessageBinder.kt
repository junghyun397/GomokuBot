package discord.interact.message

import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageAction
import core.interact.message.MessageBinder
import core.interact.message.MiniBoardStatusMap
import core.interact.message.SpotInfo
import core.interact.message.graphics.BoardStyle
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.Message
import discord.assets.EMOJI_BLACK_CIRCLE
import discord.assets.EMOJI_DARK_X
import discord.assets.EMOJI_WHITE_CIRCLE
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.RestAction
import core.assets.*
import utils.monads.Either
import utils.monads.IO
import java.io.File

object DiscordMessageBinder : MessageBinder<Message, DiscordButtons>() {

    private fun buildNextMoveEmbed(container: LanguageContainer) =
        Embed {
            color = COLOR_GREEN_HEX
            description = ":mag: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 다음 수를 놓아 주세요."
        }

    override fun bindBoard(publisher: DiscordMessagePublisher, container: LanguageContainer, board: Either<String, File>) =
        board.fold(
            onLeft = { textBoard -> IO {
                arrayOf(publisher(Message(
                    embeds = listOf(
                        Embed {
                            color = COLOR_GREEN_HEX
                            description = textBoard
                        },
                        buildNextMoveEmbed(container)
                    )
                )))
            } },
            onRight = { imageBoard -> IO {
                arrayOf(
                    publisher(Message()).addFile(imageBoard),
                    publisher(Message(
                    embeds = listOf(
                        buildNextMoveEmbed(container)
                    )))
                )
            } }
        )

    override fun bindButtons(boardAction: MessageAction<DiscordButtons>, container: LanguageContainer, commandMap: MiniBoardStatusMap) =
        boardAction.addButtons(
            commandMap.map { row -> ActionRow.of(
                row.map { element -> when (element.second) {
                    SpotInfo.FREE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", element.first)
                    SpotInfo.BLACK -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                    SpotInfo.WHITE -> Button.of(ButtonStyle.SECONDARY, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                    SpotInfo.BLACK_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_BLACK_CIRCLE).asDisabled()
                    SpotInfo.WHITE_RECENT -> Button.of(ButtonStyle.SUCCESS, "s-${element.first}", "", EMOJI_WHITE_CIRCLE).asDisabled()
                    SpotInfo.FORBIDDEN -> Button.of(ButtonStyle.DANGER, "s-${element.first}", "", EMOJI_DARK_X).asDisabled()
                } }
            ) }.reversed() // cartesian coordinate system
        )

    override fun bindAboutBot(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.helpAboutTitle()
                description = container.helpAboutDescription()
                thumbnail =
                    "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/discord/images/profile-thumbnail.jpg"
                field {
                    name = container.helpAboutDeveloper()
                    value = "junghyun397#6725"
                }
                field {
                    name = container.helpAboutRepository()
                    value = "[github/GomokuBot](https://github.com/junghyun397/GomokuBot)"
                }
                field {
                    name = container.helpAboutVersion()
                    value = "NG 1.0"
                }
                field {
                    name = container.helpAboutSupport()
                    value = "[discord.gg/vq8pkfF](https://discord.gg/vq8pkfF)"
                }
                field {
                    name = container.helpAboutInvite()
                    value = "[do1ph.in/gomokubot](https://do1ph.in/gomokubot)"
                }
                footer {
                    name = "$UNICODE_ZAP Powered by Kotlin, Project Reactor, R2DBC, gRPC, JDA"
                }
            }
        )) }

    override fun bindCommandGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.helpCommandInfo()
                description = container.helpCommandDescription()
                field {
                    name = "``/${container.helpCommand()}`` or ``~${container.helpCommand()}``"
                    value = container.helpCommandHelp()
                    inline = false
                }
                field {
                    name = "``/${container.startCommand()}`` or ``~${container.startCommand()}``"
                    value = container.helpCommandPVE()
                    inline = false
                }
                field {
                    name = "``/${container.startCommand()} @mention`` or ``~${container.startCommand()} @mention``"
                    value = container.helpCommandPVP()
                    inline = false
                }
                field {
                    name = "``/s`` or ``~s``"
                    value = "Make move" // TODO
                    inline = false
                }
                field {
                    name = "``/${container.resignCommand()}`` or ``~${container.resignCommand()}``"
                    value = container.helpCommandResign()
                    inline = false
                }
                field {
                    name = "``/${container.langCommand()}`` or ``~${container.langCommand()}``"
                    value = container.helpCommandLang(languageEnumeration)
                    inline = false
                }
                field {
                    name = "``/${container.styleCommand()}`` or ``~${container.styleCommand()}``"
                    value = container.helpCommandStyle()
                    inline = false
                }
                field {
                    name = "``/${container.rankCommand()}`` or ``~${container.rankCommand()}``"
                    value = container.helpCommandRank()
                    inline = false
                }
                field {
                    name = "``/${container.ratingCommand()}`` or ``~${container.ratingCommand()}``"
                    value = container.helpCommandRating()
                    inline = false
                }
            }
        )) }

    override fun bindStyleGuide(publisher: DiscordMessagePublisher, container: LanguageContainer) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.styleInfo()
                description = container.styleDescription()
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

    private val languageEmbed = Embed {
        color = COLOR_NORMAL_HEX
        title = "GomokuBot / Language"
        description = "The default language has been set as the server region for this channel. Please select the proper language for this server!"
        Language.values().fold(this) { builder, language ->
            builder.also { field {
                name = language.container.languageName()
                value = language.container.languageSuggestion()
                inline = false
            } }
        }
    }

    override fun bindLanguageGuide(publisher: DiscordMessagePublisher) =
        IO { publisher(Message(embed = languageEmbed)) }

    fun sendPermissionNotGrantedEmbed(publisher: (Message) -> RestAction<Message>, container: LanguageContainer, channelName: String) =
        publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "$UNICODE_CONSTRUCTION Something Wrong"
                description = "GomokuBot은 ``${channelName}``채널에 메시지를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."
                footer {
                    name = "$UNICODE_ALARM_CLOCK 이 메지시는 1분 뒤 지워집니다."
                }
            }
        ))

}
