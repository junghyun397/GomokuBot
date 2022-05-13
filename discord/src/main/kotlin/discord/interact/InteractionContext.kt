package discord.interact

import core.BotContext
import core.assets.Guild
import core.session.entities.GuildConfig
import net.dv8tion.jda.api.events.Event
import utils.assets.LinuxTime

data class InteractionContext<out E : Event>(
    val bot: BotContext,
    val event: E,
    val guild: Guild,
    val config: GuildConfig,
    val emittenTime: LinuxTime
) {

    val jdaGuild get() = this.event.jda.getGuildById(guild.id.idLong)!!

    val archiveChannel get() = this.event.jda.getGuildById(64645)!!.getTextChannelById(546465)!!

}
