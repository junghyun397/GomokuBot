package discord.interact

import core.BotContext
import core.assets.Guild
import core.session.entities.GuildConfig
import net.dv8tion.jda.api.events.Event
import utils.assets.LinuxTime

data class InteractionContext<out E : Event>(
    val botContext: BotContext,
    val event: E,
    val guild: Guild,
    val config: GuildConfig,
    val emittenTime: LinuxTime
) {

    val jdaGuild get() = this.event.jda.getGuildById(guild.id.id)!!

}
