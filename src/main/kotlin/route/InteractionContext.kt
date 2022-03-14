package route

import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.events.Event
import utility.GuildId
import utility.LinuxTime

data class InteractionContext<T : Event>(
    val botContext: BotContext,
    val event: T,
    val languageContainer: LanguageContainer,
    val guild: GuildId,
    val guildName: String,
    val emittenTime: LinuxTime
)
