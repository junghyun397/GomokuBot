package interact.message

import dev.minn.jda.ktx.Message
import interact.i18n.LanguageContainer
import utility.MessagePublisher

object MessageAgent {

    fun sendHelpAbout(messagePublisher: MessagePublisher, languageContainer: LanguageContainer) {
        messagePublisher(Message {  }).queue()
    }

    fun sendHelpCommand(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit = TODO()

    fun sendHelpSkin(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit = TODO()

    fun sendHelpLanguage(messagePublisher: MessagePublisher, languageContainer: LanguageContainer): Unit = TODO()

    fun sendGameNotFound(messagePublisher: MessagePublisher, languageContainer: LanguageContainer, nameTag: String): Unit = TODO()

}

