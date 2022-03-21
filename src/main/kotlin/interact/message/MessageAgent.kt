package interact.message

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.Message
import interact.i18n.Language
import interact.i18n.LanguageContainer
import interact.message.graphics.BoardStyle
import utility.COLOR_NORMAL_HEX
import utility.MessagePublisher

object MessageAgent {

    fun sendHelpAbout(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit =
        messagePublisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = languageContainer.helpAboutInfo()
                thumbnail = "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/profile-thumbnail.jpg"
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
                    value = "ng 1.0"
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
        )).queue()

    private val languageDelegates = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    fun sendHelpCommand(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit =
        messagePublisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = languageContainer.helpCommandInfo()
                description = languageContainer.helpCommandDescription()
                field {
                    name= "``/help`` or ``~help``"
                    value = languageContainer.helpCommandHelp()
                    inline = false
                }
                field {
                    name = "``/start`` or ``~start``"
                    value = languageContainer.helpCommandPVE()
                    inline = false
                }
                field {
                    name = "``/start @mention`` or ``~start @mention``"
                    value = languageContainer.helpCommandPVP()
                    inline = false
                }
                field {
                    name = "``/s`` or ``~s``"
                    value = "make move" // TODO
                    inline = false
                }
                field {
                    name = "``/resign`` or ``~resign``"
                    value = languageContainer.helpCommandResign()
                    inline = false
                }
                field {
                    name = "``/lang`` or ``~lang``"
                    value = languageContainer.helpCommandLang(languageDelegates) // TODO
                    inline = false
                }
                field {
                    name = "``/style`` or ``~style``"
                    value = languageContainer.helpCommandStyle()
                    inline = false
                }
                field {
                    name = "``/rank`` or ``~rank``"
                    value = languageContainer.helpCommandRank()
                    inline = false
                }
                field {
                    name = "``/rating`` or ``~rating``"
                    value = languageContainer.helpCommandRating()
                    inline = false
                }
            }
        )).queue()

    fun sendHelpStyle(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit =
        messagePublisher(Message(
            embed = Embed {
                color = COLOR_NORMAL_HEX
                title = languageContainer.styleInfo()
                description = languageContainer.styleDescription()
                BoardStyle.values()
                    .drop(1)
                    .fold(this) { builder, style ->
                        builder.also { field {
                            name = "Style ``${style.sample.styleName}``"
                            value = style.sample.sampleView
                            inline = false
                        } }
                    }
                field {
                    name = "Style ``IMAGE``"
                    inline = false
                }
                image = "https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/style-preview.jpg"
            }
        )).queue()

    private val languageEmbed = Embed {
        color = COLOR_NORMAL_HEX
        title = "Language guide"
        description = "The default language has been set as the server region for this channel. Please select the proper language for this server!"
        Language.values().fold(this) { builder, language ->
            builder.also { field {
                name = language.container.languageName()
                value = language.container.languageSuggestion()
                inline = false
            } }
        }
    }

    fun sendHelpLanguage(messagePublisher: MessagePublisher): Unit =
        messagePublisher(Message(embed = languageEmbed)).queue()

}
