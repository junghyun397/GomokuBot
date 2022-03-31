package discord.interact

import core.BotContext
import core.session.entities.GuildConfig
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.Event
import utils.values.LinuxTime

data class InteractionContext<out E : Event>(
    val botContext: BotContext,
    val event: E,
    val config: GuildConfig,
    val guild: Guild,
    val guildName: String,
    val emittenTime: LinuxTime
)
