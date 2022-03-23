package interact.message

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.Message
import interact.i18n.Language
import interact.i18n.LanguageContainer
import interact.message.graphics.BoardStyle
import utility.COLOR_NORMAL_HEX
import utility.MessagePublisher

object MessageAgent {

    fun sendEmbedAbout(messagePublisher: MessagePublisher, languageContainer: LanguageContainer) =
        messagePublisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = languageContainer.helpAboutTitle()
                description = languageContainer.helpAboutDescription()
                thumbnail =
                    "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/profile-thumbnail.jpg"
                field {
                    name = languageContainer.helpAboutDeveloper()
                    value = "junghyun397#6725"
                }
                field {
                    name = languageContainer.helpAboutRepository()
                    value = "[github/GomokuBot](https://github.com/junghyun397/GomokuBot)"
                }
                field {
                    name = languageContainer.helpAboutVersion()
                    value = "NG 1.0"
                }
                field {
                    name = languageContainer.helpAboutSupport()
                    value = "[discord.gg/vq8pkfF](https://discord.gg/vq8pkfF)"
                }
                field {
                    name = languageContainer.helpAboutInvite()
                    value = "[do1ph.in/gomokubot](https://do1ph.in/gomokubot)"
                }
            }
        )).map { it.queue() }

    private val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    fun sendEmbedCommand(messagePublisher: MessagePublisher, languageContainer: LanguageContainer) =
        messagePublisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = languageContainer.helpCommandInfo()
                description = languageContainer.helpCommandDescription()
                field {
                    name = "``/${languageContainer.helpCommand()}`` or ``~${languageContainer.helpCommand()}``"
                    value = languageContainer.helpCommandHelp()
                    inline = false
                }
                field {
                    name = "``/${languageContainer.startCommand()}`` or ``~${languageContainer.startCommand()}``"
                    value = languageContainer.helpCommandPVE()
                    inline = false
                }
                field {
                    name =
                        "``/${languageContainer.startCommand()} @mention`` or ``~${languageContainer.startCommand()} @mention``"
                    value = languageContainer.helpCommandPVP()
                    inline = false
                }
                field {
                    name = "``/s`` or ``~s``"
                    value = "Make move" // TODO
                    inline = false
                }
                field {
                    name = "``/${languageContainer.resignCommand()}`` or ``~${languageContainer.resignCommand()}``"
                    value = languageContainer.helpCommandResign()
                    inline = false
                }
                field {
                    name = "``/${languageContainer.langCommand()}`` or ``~${languageContainer.langCommand()}``"
                    value = languageContainer.helpCommandLang(languageEnumeration) // TODO
                    inline = false
                }
                field {
                    name = "``/${languageContainer.styleCommand()}`` or ``~${languageContainer.styleCommand()}``"
                    value = languageContainer.helpCommandStyle()
                    inline = false
                }
                field {
                    name = "``/${languageContainer.rankCommand()}`` or ``~${languageContainer.rankCommand()}``"
                    value = languageContainer.helpCommandRank()
                    inline = false
                }
                field {
                    name = "``/${languageContainer.ratingCommand()}`` or ``~${languageContainer.ratingCommand()}``"
                    value = languageContainer.helpCommandRating()
                    inline = false
                }
            }
        )).map { it.queue() }

    fun sendEmbedStyle(messagePublisher: MessagePublisher, languageContainer: LanguageContainer) =
        messagePublisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = languageContainer.styleInfo()
                description = languageContainer.styleDescription()
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
        )).map { it.queue() }

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

    fun sendEmbedLanguage(messagePublisher: MessagePublisher) =
        messagePublisher(Message(embed = languageEmbed)).map { it.queue() }

}
