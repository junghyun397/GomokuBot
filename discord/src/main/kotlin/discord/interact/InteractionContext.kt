package discord.interact

import core.interact.ExecutionContext
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.sharding.ShardManager

interface InteractionContext<out E : Event> : ExecutionContext {

    val discordConfig: DiscordConfig

    val event: E

    val shardManager: ShardManager

    val jdaChannel get() = this.event.jda.getGuildById(channel.givenId.idLong)!!

}
