package discord.interact

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.session.entities.GuildConfig
import net.dv8tion.jda.api.events.Event
import utils.assets.LinuxTime

data class InteractionContext<out E : Event>(
    val bot: BotContext,
    val discordConfig: DiscordConfig,
    val event: E,
    val user: User,
    val guild: Guild,
    val config: GuildConfig,
    val emittedTime: LinuxTime
) {

    val jdaGuild get() = this.event.jda.getGuildById(guild.givenId.idLong)!!

}
