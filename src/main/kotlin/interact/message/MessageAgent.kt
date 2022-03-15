package interact.message

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.Message
import interact.i18n.LanguageContainer
import utility.MessagePublisher
import utility.colorNormalHex

object MessageAgent {

    fun sendHelpAbout(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit =
        messagePublisher(Message(
            embed = Embed {
                title = languageContainer.helpAboutInfo()
                description = languageContainer.helpAboutDescription()
                thumbnail = "https://github.com/junghyun397/GomokuBot/blob/master/images/profile-thumbnail.jpg?raw=true"
                color = colorNormalHex

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
                    value = "neo 1.0"
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

    fun sendHelpCommand(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit =
        messagePublisher(Message(
            embed = Embed {
                title = languageContainer.helpCommandInfo()
                description = languageContainer.helpCommandDescription()
                color = colorNormalHex
                field {
                    name = "/help or ~help"
                    value = languageContainer.helpCommandHelp()
                    inline = false
                }
                field {
                    name = "/start or ~start"
                    value = languageContainer.helpCommandPVE()
                    inline = false
                }
                field {
                    name = "/start @mention or ~start @mention"
                    value = languageContainer.helpCommandPVP()
                    inline = false
                }
                field {
                    name = "/s or ~s"
                    value = "make move" // TODO
                    inline = false
                }
                field {
                    name = "/resign or ~resign"
                    value = languageContainer.helpCommandResign()
                    inline = false
                }
                field {
                    name = "/lang or ~lang"
                    value = languageContainer.helpCommandLang("ENG") // TODO
                    inline = false
                }
                field {
                    name = "/style or ~style"
                    value = languageContainer.helpCommandStyle()
                    inline = false
                }
                field {
                    name = "/rank or ~rank"
                    value = languageContainer.helpCommandRank()
                    inline = false
                }
                field {
                    name = "/rating or ~rating"
                    value = languageContainer.helpCommandRating()
                    inline = false
                }
            }
        )).queue()

    fun sendHelpStyle(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit = Unit // TODO

    fun sendHelpLanguage(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit = Unit // TODO

}
