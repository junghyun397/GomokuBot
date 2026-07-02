package discord.interact

import core.BotContext
import core.assets.Channel
import core.database.repositories.ChannelProfileRepository
import core.session.SessionManager
import core.session.entities.ChannelConfig
import discord.assets.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.sharding.ShardManager
import kotlin.time.Clock
import kotlin.time.Instant

data class InternalInteractionContext<out E : Event> (
    override val bot: BotContext,
    override val shardManager: ShardManager,
    override val discordConfig: DiscordConfig,
    override val event: E,
    override val channel: Channel,
    override val config: ChannelConfig,
    override val emittedTime: Instant,
    override val source: String
) : InteractionContext<E> {

    companion object {

        suspend fun <E: Event> fromJDAEvent(bot: BotContext, discordConfig: DiscordConfig, shardManager: ShardManager, event: E, jdaChannel: JDAChannel): InternalInteractionContext<E> {
            val channel = ChannelProfileRepository.retrieveOrInsertChannel(bot.dbConnection, DISCORD_PLATFORM_ID, jdaChannel.channelId()) {
                jdaChannel.profile()
            }

            return InternalInteractionContext(
                bot = bot,
                shardManager = shardManager,
                discordConfig = discordConfig,
                event = event,
                channel = channel,
                config = SessionManager.retrieveChannelConfig(bot.sessions, channel),
                emittedTime = Clock.System.now(),
                source = event.abbreviation()
            )
        }
    }
}
