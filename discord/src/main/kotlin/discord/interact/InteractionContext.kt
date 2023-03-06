package discord.interact

import core.interact.ExecutionContext
import net.dv8tion.jda.api.events.Event

interface InteractionContext<out E : Event> : ExecutionContext {

    val discordConfig: DiscordConfig

    val event: E

    val jdaGuild get() = this.event.jda.getGuildById(guild.givenId.idLong)!!

}
