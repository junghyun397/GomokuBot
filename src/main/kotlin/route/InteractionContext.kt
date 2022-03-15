package route

import net.dv8tion.jda.api.events.Event
import session.entities.GuildConfig
import utility.GuildId
import utility.LinuxTime

data class InteractionContext<T : Event>(
    val botContext: BotContext,
    val event: T,
    val guildConfig: GuildConfig,
    val guild: GuildId,
    val guildName: String,
    val emittenTime: LinuxTime
)
