package interact.message

import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.Channel

object MessageAgent {

    private val guildMap: HashMap<Long, LanguageContainer> = hashMapOf()

    fun updateGuildLang(guildId: Long, languageContainer: LanguageContainer) =
        guildMap.putIfAbsent(guildId, languageContainer)

    fun sendHelpAbout(channel: Channel): Unit = TODO()

    fun sendHelpCommand(channel: Channel): Unit = TODO()

    fun sendHelpSkin(channel: Channel): Unit = TODO()

    fun sendHelpLanguage(channel: Channel): Unit = TODO()

}