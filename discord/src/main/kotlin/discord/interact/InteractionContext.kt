package discord.interact

import core.BotContext
import core.session.entities.GuildConfig
import net.dv8tion.jda.api.events.Event
import utils.values.GuildId
import utils.values.LinuxTime

class InteractionContext<out E : Event>(
    val botContext: BotContext,
    val event: E,
    val guildConfig: GuildConfig,
    val guild: GuildId,
    val guildName: String,
    val emittenTime: LinuxTime
)
