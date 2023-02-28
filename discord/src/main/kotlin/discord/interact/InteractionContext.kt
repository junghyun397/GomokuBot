package discord.interact

import core.BotContext
import core.assets.Guild
import core.session.entities.GuildConfig
import net.dv8tion.jda.api.events.Event
import utils.assets.LinuxTime

interface InteractionContext<out E : Event> {

    val bot: BotContext

    val discordConfig: DiscordConfig

    val event: E

    val guild: Guild

    val config: GuildConfig

    val emittedTime: LinuxTime

    val jdaGuild get() = this.event.jda.getGuildById(guild.givenId.idLong)!!

}
