package route

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.Event
import utility.GuildId
import utility.LinuxTime

data class InteractionContext<T : Event>(
    val botContext: BotContext,
    val event: T,
    val guildId: GuildId,
    val guildName: String,
    val emittenTime: LinuxTime
) {

    companion object {

        fun <T : Event> of(botContext: BotContext, event: T, guild: Guild) = InteractionContext(
            botContext,
            event,
            GuildId(guild.idLong),
            guild.name,
            LinuxTime(System.currentTimeMillis())
        )

    }

}