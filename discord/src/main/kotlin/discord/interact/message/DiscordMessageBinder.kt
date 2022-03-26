package discord.interact.message

import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageBinder
import core.interact.message.graphics.BoardStyle
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.Message
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.RestAction
import utils.assets.COLOR_NORMAL_HEX
import utils.assets.COLOR_RED_HEX
import utils.monads.IO

object DiscordMessageBinder : MessageBinder<Message, DiscordButtons>() {

    override fun bindAboutBot(container: LanguageContainer, publisher: DiscordMessagePublisher) =
        IO { publisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = container.helpAboutTitle()
                description = container.helpAboutDescription()
                thumbnail =
                    "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/profile-thumbnail.jpg"
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
            }
        )).send() }

    override fun bindCommandGuide(container: LanguageContainer, publisher: DiscordMessagePublisher) =
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
                    name =
                        "``/${container.startCommand()}`` or ``~${container.startCommand()}``"
                    value = container.helpCommandPVE()
                    inline = false
                }
                field {
                    name =
                        "``/${container.startCommand()} @mention`` or ``~${container.startCommand()} @mention``"
                    value = container.helpCommandPVP()
                    inline = false
                }
                field {
                    name = "``/s`` or ``~s``"
                    value = "Make move" // TODO
                    inline = false
                }
                field {
                    name =
                        "``/${container.resignCommand()}`` or ``~${container.resignCommand()}``"
                    value = container.helpCommandResign()
                    inline = false
                }
                field {
                    name = "``/${container.langCommand()}`` or ``~${container.langCommand()}``"
                    value = container.helpCommandLang(languageEnumeration) // TODO
                    inline = false
                }
                field {
                    name =
                        "``/${container.styleCommand()}`` or ``~${container.styleCommand()}``"
                    value = container.helpCommandStyle()
                    inline = false
                }
                field {
                    name = "``/${container.rankCommand()}`` or ``~${container.rankCommand()}``"
                    value = container.helpCommandRank()
                    inline = false
                }
                field {
                    name =
                        "``/${container.ratingCommand()}`` or ``~${container.ratingCommand()}``"
                    value = container.helpCommandRating()
                    inline = false
                }
            }
        )).send() }

    override fun bindStyleGuide(container: LanguageContainer, publisher: DiscordMessagePublisher) =
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
                image = "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/style-preview.jpg"
            }
        )).send() }

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
        IO { publisher(Message(embed = languageEmbed)).send() }

    fun sendPermissionNotGrantedEmbed(container: LanguageContainer, publisher: (Message) -> RestAction<Message>, channelName: String) =
        publisher(Message(
            embed = Embed {
                color = COLOR_RED_HEX
                title = "\uD83D\uDEA7 Something Wrong"
                description = "GomokuBot은 ``${channelName}``채널에 메시지를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."
                footer {
                    name = "⏰ 이 메지시는 1분 뒤 지워집니다."
                }
            }
        ))

//    fun attachBoardWithButtons(messagePublisher: MessagePublisher) =
//        messagePublisher(Message(
//            embeds = listOf(
//                Embed {
//                    color = COLOR_GREEN_HEX
//                    description = ":mag: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 다음 수를 놓아 주세요."
//                }
//            )
//        )).fold(
//            onDefined = { it.addActionRow(
//                ActionRow.of(
//                    Button.of(ButtonStyle.SECONDARY, "e1", "e1"),
//                    Button.of(ButtonStyle.SECONDARY, "e2", "", EMOJI_BLACK_CIRCLE).asDisabled(),
//                    Button.of(ButtonStyle.SECONDARY, "e3", "", EMOJI_WHITE_CIRCLE).asDisabled(),
//                    Button.of(ButtonStyle.SECONDARY, "e4", "e4"),
//                    Button.of(ButtonStyle.SECONDARY, "e5", "e5"),
//                ),
//                ActionRow.of(
//                    Button.of(ButtonStyle.SECONDARY, "d1", "d1"),
//                    Button.of(ButtonStyle.SECONDARY, "d2", "", EMOJI_WHITE_CIRCLE).asDisabled(),
//                    Button.of(ButtonStyle.SECONDARY, "d3", "", EMOJI_WHITE_CIRCLE).asDisabled(),
//                    Button.of(ButtonStyle.SECONDARY, "d4", "", EMOJI_BLACK_CIRCLE).asDisabled(),
//                    Button.of(ButtonStyle.SECONDARY, "d5", "d5"),
//                ),
//                ActionRow.of(
//                    Button.of(ButtonStyle.SECONDARY, "c1", "c1"),
//                    Button.of(ButtonStyle.SECONDARY, "c2", "c2"),
//                    Button.of(ButtonStyle.SECONDARY, "c3", "", EMOJI_BLACK_CIRCLE).asDisabled(),
//                    Button.of(ButtonStyle.SECONDARY, "c4", "c4"),
//                    Button.of(ButtonStyle.SECONDARY, "c5", "c5"),
//                ),
//                ActionRow.of(
//                    Button.of(ButtonStyle.SECONDARY, "b1", "b1"),
//                    Button.of(ButtonStyle.SECONDARY, "b2", "b2"),
//                    Button.of(ButtonStyle.SECONDARY, "b3", "b3"),
//                    Button.of(ButtonStyle.SECONDARY, "b4", "b4"),
//                    Button.of(ButtonStyle.SECONDARY, "b5", "b5"),
//                ),
//                ActionRow.of(
//                    Button.of(ButtonStyle.SECONDARY, "a1", "a1"),
//                    Button.of(ButtonStyle.SECONDARY, "a2", "a2"),
//                    Button.of(ButtonStyle.SECONDARY, "a3", "a3"),
//                    Button.of(ButtonStyle.SECONDARY, "a4", "a4"),
//                    Button.of(ButtonStyle.SECONDARY, "a5", "a5"),
//                )
//            ).queue() },
//            onEmpty = { }
//        )

}
